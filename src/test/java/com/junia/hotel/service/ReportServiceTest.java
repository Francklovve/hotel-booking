package com.junia.hotel.service;

import com.junia.hotel.exception.BookingException;
import com.junia.hotel.model.Reservation;
import com.junia.hotel.model.room.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.junia.hotel.TestData.stay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportServiceTest {

    private HotelFixture hotel;

    @BeforeEach
    void setUp() {
        hotel = new HotelFixture();
    }

    @Test
    void emptyHotelHasNoRevenueAndNoBestCustomer() {
        assertEquals(0, hotel.reportService.totalRevenue(), 0.001);
        assertTrue(hotel.reportService.bestCustomer().isEmpty());
    }

    @Test
    void revenueIsGroupedByRoomType() throws BookingException {
        hotel.reservationService.book("G003", "102", stay("2026-12-14", "2026-12-16"), 1);
        hotel.reservationService.book("G003", "201", stay("2026-12-14", "2026-12-15"), 1);

        Map<RoomType, Double> revenue = hotel.reportService.revenueByRoomType();

        assertEquals(160, revenue.get(RoomType.STANDARD), 0.001);
        assertEquals(200, revenue.get(RoomType.SUITE), 0.001);
    }

    @Test
    void cancelledReservationsAreNotCountedInRevenue() throws BookingException {
        Reservation reservation = hotel.reservationService.book("G003", "102", stay("2026-12-14", "2026-12-16"), 1);
        hotel.reservationService.cancel(reservation.getId());

        assertEquals(0, hotel.reportService.totalRevenue(), 0.001);
    }

    @Test
    void occupancyRateCountsOnlyNightsInsideThePeriod() throws BookingException {
        // 3 rooms * 10 nights = 30 room-nights; the booking has 4 nights, 2 of them in the period
        hotel.reservationService.book("G003", "102", stay("2026-12-08", "2026-12-12"), 1);

        double rate = hotel.reportService.occupancyRate(stay("2026-12-10", "2026-12-20"));

        assertEquals(2.0 / 30, rate, 0.0001);
    }

    @Test
    void bestCustomerIsTheGuestWhoSpentTheMost() throws BookingException {
        hotel.reservationService.book("G003", "102", stay("2026-12-14", "2026-12-15"), 1);
        hotel.reservationService.book("G001", "201", stay("2026-12-14", "2026-12-16"), 1);

        assertEquals("G001", hotel.reportService.bestCustomer().orElseThrow().getId());
    }
}
