package com.junia.hotel.service;

import com.junia.hotel.HotelFixture;
import com.junia.hotel.exception.BookingException;
import com.junia.hotel.exception.CapacityExceededException;
import com.junia.hotel.exception.GuestNotFoundException;
import com.junia.hotel.exception.InvalidDateRangeException;
import com.junia.hotel.exception.ReservationNotFoundException;
import com.junia.hotel.exception.RoomNotFoundException;
import com.junia.hotel.exception.RoomUnavailableException;
import com.junia.hotel.model.Reservation;
import com.junia.hotel.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.junia.hotel.TestData.stay;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationServiceTest {

    private HotelFixture hotel;
    private ReservationService service;

    @BeforeEach
    void setUp() {
        hotel = new HotelFixture();
        service = hotel.reservationService;
    }

    @Test
    void bookingAFreeRoomCreatesAConfirmedReservation() throws BookingException {
        Reservation reservation = service.book("G003", "102", stay("2026-12-10", "2026-12-12"), 2);

        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals(160, reservation.getTotalPrice(), 0.001);
        assertEquals(1, hotel.reservations.count());
    }

    @Test
    void bookingAppliesTheGuestDiscount() throws BookingException {
        Reservation reservation = service.book("G001", "102", stay("2026-12-10", "2026-12-12"), 2);

        assertEquals(144, reservation.getTotalPrice(), 0.001);
    }

    @Test
    void eachReservationGetsADifferentId() throws BookingException {
        Reservation first = service.book("G003", "102", stay("2026-12-10", "2026-12-12"), 1);
        Reservation second = service.book("G003", "201", stay("2026-12-10", "2026-12-12"), 1);

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void overlappingBookingOnTheSameRoomIsRefused() throws BookingException {
        service.book("G001", "201", stay("2026-12-24", "2026-12-27"), 2);

        RoomUnavailableException exception = assertThrows(RoomUnavailableException.class,
                () -> service.book("G003", "201", stay("2026-12-26", "2026-12-28"), 2));
        assertTrue(exception.getMessage().contains("201"));
    }

    @Test
    void refusedBookingIsNotRecorded() throws BookingException {
        service.book("G001", "201", stay("2026-12-24", "2026-12-27"), 2);

        assertThrows(RoomUnavailableException.class,
                () -> service.book("G003", "201", stay("2026-12-26", "2026-12-28"), 2));
        assertEquals(1, hotel.reservations.count());
    }

    @Test
    void bookingStartingOnPreviousCheckOutDayIsAccepted() throws BookingException {
        service.book("G001", "201", stay("2026-12-24", "2026-12-27"), 2);

        assertDoesNotThrow(() -> service.book("G003", "201", stay("2026-12-27", "2026-12-29"), 2));
    }

    @Test
    void sameDatesInAnotherRoomAreAccepted() throws BookingException {
        service.book("G001", "201", stay("2026-12-24", "2026-12-27"), 2);

        assertDoesNotThrow(() -> service.book("G003", "102", stay("2026-12-24", "2026-12-27"), 2));
    }

    @Test
    void tooManyGuestsForTheRoomIsRefused() {
        assertThrows(CapacityExceededException.class,
                () -> service.book("G003", "102", stay("2026-12-10", "2026-12-12"), 3));
    }

    @Test
    void bookingExactlyAtFullCapacityIsAccepted() {
        assertDoesNotThrow(() -> service.book("G003", "301", stay("2026-12-10", "2026-12-12"), 4));
    }

    @Test
    void unknownGuestIsRefused() {
        assertThrows(GuestNotFoundException.class,
                () -> service.book("G999", "102", stay("2026-12-10", "2026-12-12"), 1));
    }

    @Test
    void unknownRoomIsRefused() {
        RoomNotFoundException exception = assertThrows(RoomNotFoundException.class,
                () -> service.book("G003", "999", stay("2026-12-10", "2026-12-12"), 1));
        assertEquals("No room with number 999", exception.getMessage());
    }

    @Test
    void bookingInThePastIsRefused() {
        assertThrows(InvalidDateRangeException.class,
                () -> service.book("G003", "102", stay("2026-11-20", "2026-11-22"), 1));
    }

    @Test
    void bookingStartingTodayIsAccepted() {
        assertDoesNotThrow(() -> service.book("G003", "102", stay("2026-12-01", "2026-12-02"), 1));
    }

    @Test
    void importedHistoricalReservationMayBeInThePast() {
        assertDoesNotThrow(() -> service.importExisting("G003", "102", stay("2026-11-20", "2026-11-22"), 1));
    }

    @Test
    void cancellingFreesTheRoom() throws BookingException {
        Reservation reservation = service.book("G001", "201", stay("2026-12-24", "2026-12-27"), 2);

        service.cancel(reservation.getId());

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        assertDoesNotThrow(() -> service.book("G003", "201", stay("2026-12-24", "2026-12-27"), 2));
    }

    @Test
    void cancellingTwiceIsRefused() throws BookingException {
        Reservation reservation = service.book("G001", "201", stay("2026-12-24", "2026-12-27"), 2);
        service.cancel(reservation.getId());

        assertThrows(IllegalStateException.class, () -> service.cancel(reservation.getId()));
    }

    @Test
    void cancellingUnknownReservationIsRefused() {
        assertThrows(ReservationNotFoundException.class, () -> service.cancel("R9999"));
    }

    @Test
    void findsOnlyTheReservationsOfTheGivenGuest() throws BookingException {
        service.book("G001", "201", stay("2026-12-24", "2026-12-27"), 2);
        service.book("G003", "102", stay("2026-12-24", "2026-12-27"), 2);

        assertEquals(1, service.findByGuest("G001").size());
    }
}
