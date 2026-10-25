package com.junia.hotel.service;

import com.junia.hotel.exception.BookingException;
import com.junia.hotel.model.Reservation;
import com.junia.hotel.model.room.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static com.junia.hotel.TestData.stay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportSnapshotTest {

    private HotelFixture hotel;

    @BeforeEach
    void setUp() {
        hotel = new HotelFixture();
    }

    @Test
    void reportKeepsGlobalRevenueAndBestCustomerWhileOccupancyUsesThePeriod() throws BookingException {
        hotel.reservationService.book("G003", "102", stay("2026-12-08", "2026-12-12"), 1);
        hotel.reservationService.book("G001", "201", stay("2026-12-14", "2026-12-16"), 1);

        Report report = hotel.reportService.buildReport(stay("2026-12-10", "2026-12-13"));

        assertEquals(320.0, report.revenueByRoomType().get(RoomType.STANDARD), 0.001);
        assertEquals(360.0, report.revenueByRoomType().get(RoomType.SUITE), 0.001);
        assertEquals(680.0, report.totalRevenue(), 0.001);
        assertEquals(0.222222, report.occupancyRate(), 0.000001);
        assertEquals("Alice", report.bestCustomerName());
    }

    @Test
    void cancelledReservationsAreExcludedFromEveryReportIndicator() throws BookingException {
        hotel.reservationService.book("G003", "102", stay("2026-12-14", "2026-12-16"), 1);
        Reservation suiteReservation = hotel.reservationService.book(
                "G001", "201", stay("2026-12-14", "2026-12-16"), 1);
        hotel.reservationService.cancel(suiteReservation.getId());

        Report report = hotel.reportService.buildReport(stay("2026-12-14", "2026-12-16"));

        assertEquals(160.0, report.totalRevenue(), 0.001);
        assertEquals(160.0, report.revenueByRoomType().get(RoomType.STANDARD), 0.001);
        assertFalse(report.revenueByRoomType().containsKey(RoomType.SUITE));
        assertEquals(0.333333, report.occupancyRate(), 0.000001);
        assertEquals("Chloe", report.bestCustomerName());
    }

    @Test
    void reportWithoutReservationsHasZeroIndicatorsAndNoBestCustomer() {
        Report report = hotel.reportService.buildReport(stay("2026-12-10", "2026-12-12"));

        assertTrue(report.revenueByRoomType().isEmpty());
        assertEquals(0.0, report.totalRevenue(), 0.001);
        assertEquals(0.0, report.occupancyRate(), 0.000001);
        assertEquals("None", report.bestCustomerName());
    }

    @Test
    void laterBookingsDoNotChangeAnAlreadyBuiltReport() throws BookingException {
        hotel.reservationService.book("G003", "102", stay("2026-12-14", "2026-12-16"), 1);
        Report report = hotel.reportService.buildReport(stay("2026-12-14", "2026-12-16"));

        hotel.reservationService.book("G003", "201", stay("2026-12-14", "2026-12-15"), 1);

        assertEquals(160.0, report.totalRevenue(), 0.001);
        assertEquals(160.0, report.revenueByRoomType().get(RoomType.STANDARD), 0.001);
        assertFalse(report.revenueByRoomType().containsKey(RoomType.SUITE));
        assertEquals(0.333333, report.occupancyRate(), 0.000001);
    }

    @Test
    void reportCopiesItsRevenueMapAndExposesItReadOnly() {
        Map<RoomType, Double> revenues = new EnumMap<>(RoomType.class);
        revenues.put(RoomType.STANDARD, 50.0);
        Report report = new Report(stay("2026-12-10", "2026-12-12"), revenues, 50.0, 0.5, "Chloe");

        revenues.put(RoomType.STANDARD, 999.0);

        assertEquals(50.0, report.revenueByRoomType().get(RoomType.STANDARD), 0.001);
        assertThrows(UnsupportedOperationException.class,
                () -> report.revenueByRoomType().put(RoomType.SUITE, 200.0));
    }

    @Test
    void reportAndBuilderRejectMissingRequiredData() {
        assertThrows(IllegalArgumentException.class, () -> hotel.reportService.buildReport(null));
        assertThrows(IllegalArgumentException.class,
                () -> new Report(null, Map.of(), 0.0, 0.0, "None"));
        assertThrows(IllegalArgumentException.class,
                () -> new Report(stay("2026-12-10", "2026-12-12"), null, 0.0, 0.0, "None"));
        assertThrows(IllegalArgumentException.class,
                () -> new Report(stay("2026-12-10", "2026-12-12"), Map.of(), 0.0, 0.0, null));
    }
}
