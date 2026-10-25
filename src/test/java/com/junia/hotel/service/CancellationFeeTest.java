package com.junia.hotel.service;

import com.junia.hotel.exception.BookingException;
import com.junia.hotel.exception.ReservationNotFoundException;
import com.junia.hotel.model.DateRange;
import com.junia.hotel.model.Reservation;
import com.junia.hotel.model.ReservationStatus;
import com.junia.hotel.model.room.StandardRoom;
import com.junia.hotel.pricing.CancellationPolicy;
import com.junia.hotel.pricing.CancellationPolicyResolver;
import com.junia.hotel.pricing.FlexibleCancellationPolicy;
import com.junia.hotel.pricing.NonRefundablePolicy;
import com.junia.hotel.pricing.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static com.junia.hotel.TestData.stay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CancellationFeeTest {

    private HotelFixture hotel;

    @BeforeEach
    void setUp() {
        hotel = new HotelFixture();
    }

    @Test
    void earlyStandardCancellationRefundsTheWholePriceAndCancelsTheReservation() throws BookingException {
        Reservation reservation = hotel.reservationService.book(
                "G003", "102", stay("2026-12-10", "2026-12-12"), 2);

        CancellationResult result = hotel.reservationService.cancel(reservation.getId());

        assertSame(reservation, result.reservation());
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        assertEquals(0.0, result.fee(), 0.001);
        assertEquals(160.0, result.refund(), 0.001);
    }

    @Test
    void standardCancellationExactlyTwoDaysBeforeArrivalIsFree() throws BookingException {
        Reservation reservation = hotel.reservationService.book(
                "G003", "102", stay("2026-12-03", "2026-12-05"), 2);

        CancellationResult result = hotel.reservationService.cancel(reservation.getId());

        assertEquals(0.0, result.fee(), 0.001);
        assertEquals(160.0, result.refund(), 0.001);
    }

    @Test
    void standardCancellationOneDayBeforeArrivalCostsHalfThePrice() throws BookingException {
        Reservation reservation = hotel.reservationService.book(
                "G003", "102", stay("2026-12-02", "2026-12-04"), 2);

        CancellationResult result = hotel.reservationService.cancel(reservation.getId());

        assertEquals(80.0, result.fee(), 0.001);
        assertEquals(80.0, result.refund(), 0.001);
    }

    @Test
    void suiteCancellationCostsTheWholePriceEvenWellBeforeArrival() throws BookingException {
        Reservation reservation = hotel.reservationService.book(
                "G003", "201", stay("2026-12-14", "2026-12-15"), 2);

        CancellationResult result = hotel.reservationService.cancel(reservation.getId());

        assertEquals(200.0, result.fee(), 0.001);
        assertEquals(0.0, result.refund(), 0.001);
    }

    @Test
    void familyCancellationUsesTheFlexiblePolicy() throws BookingException {
        Reservation reservation = hotel.reservationService.book(
                "G003", "301", stay("2026-12-02", "2026-12-04"), 4);

        CancellationResult result = hotel.reservationService.cancel(reservation.getId());

        assertEquals(170.0, result.fee(), 0.001);
        assertEquals(170.0, result.refund(), 0.001);
    }

    @Test
    void cancellationFeeUsesTheDiscountedPriceFrozenAtBooking() throws BookingException {
        Reservation reservation = hotel.reservationService.book(
                "G001", "102", stay("2026-12-02", "2026-12-04"), 2);

        CancellationResult result = hotel.reservationService.cancel(reservation.getId());

        assertEquals(72.0, result.fee(), 0.001);
        assertEquals(72.0, result.refund(), 0.001);
    }

    @Test
    void feeIsRoundedToCentsAndRefundKeepsTheOriginalTotal() throws BookingException {
        hotel.rooms.save(new StandardRoom("103", 2, 80.01));
        Reservation reservation = hotel.reservationService.book(
                "G003", "103", stay("2026-12-02", "2026-12-03"), 1);

        CancellationResult result = hotel.reservationService.cancel(reservation.getId());

        assertEquals(40.01, result.fee(), 0.001);
        assertEquals(40.0, result.refund(), 0.001);
    }

    @Test
    void cancellingTwiceStillRefusesTheSecondCancellation() throws BookingException {
        Reservation reservation = hotel.reservationService.book(
                "G003", "102", stay("2026-12-10", "2026-12-12"), 1);
        hotel.reservationService.cancel(reservation.getId());

        assertThrows(IllegalStateException.class,
                () -> hotel.reservationService.cancel(reservation.getId()));
    }

    @Test
    void cancellingAnUnknownReservationStillReportsTheMissingId() {
        assertThrows(ReservationNotFoundException.class,
                () -> hotel.reservationService.cancel("R9999"));
    }

    @Test
    void cancellationUsesTheResolverProvidedToTheService() throws BookingException {
        CancellationPolicy quarterPricePolicy = new CancellationPolicy() {
            @Override
            public String name() {
                return "Quarter price";
            }

            @Override
            public double feeRate(DateRange stay, LocalDate cancellationDate) {
                return 0.25;
            }
        };
        CancellationPolicyResolver resolver = new CancellationPolicyResolver(
                quarterPricePolicy, new NonRefundablePolicy());
        Clock clock = Clock.fixed(LocalDate.of(2026, 12, 1)
                .atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        ReservationService service = new ReservationService(hotel.rooms, hotel.guests, hotel.reservations,
                hotel.availabilityService, new PricingService(List.of()), resolver, clock);
        Reservation reservation = service.book("G003", "102", stay("2026-12-10", "2026-12-12"), 1);

        CancellationResult result = service.cancel(reservation.getId());

        assertEquals(40.0, result.fee(), 0.001);
        assertEquals(120.0, result.refund(), 0.001);
    }

    @Test
    void cancellationDateComesFromTheInjectedClock() throws BookingException {
        Reservation reservation = hotel.reservationService.book(
                "G003", "102", stay("2026-12-04", "2026-12-06"), 1);
        Clock laterClock = Clock.fixed(LocalDate.of(2026, 12, 3)
                .atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        ReservationService laterService = new ReservationService(hotel.rooms, hotel.guests, hotel.reservations,
                hotel.availabilityService, new PricingService(List.of()),
                new CancellationPolicyResolver(new FlexibleCancellationPolicy(), new NonRefundablePolicy()),
                laterClock);

        CancellationResult result = laterService.cancel(reservation.getId());

        assertEquals(80.0, result.fee(), 0.001);
        assertEquals(80.0, result.refund(), 0.001);
    }
}
