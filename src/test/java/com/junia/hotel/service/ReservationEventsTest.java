package com.junia.hotel.service;

import com.junia.hotel.HotelFixture;
import com.junia.hotel.exception.BookingException;
import com.junia.hotel.exception.CapacityExceededException;
import com.junia.hotel.exception.DataLoadingException;
import com.junia.hotel.exception.GuestNotFoundException;
import com.junia.hotel.exception.InvalidDateRangeException;
import com.junia.hotel.exception.ReservationNotFoundException;
import com.junia.hotel.exception.RoomNotFoundException;
import com.junia.hotel.exception.RoomUnavailableException;
import com.junia.hotel.io.CsvHotelDataSource;
import com.junia.hotel.model.Reservation;
import com.junia.hotel.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.junia.hotel.TestData.stay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationEventsTest {

    private HotelFixture hotel;
    private CountingListener listener;

    @BeforeEach
    void setUp() {
        hotel = new HotelFixture();
        listener = new CountingListener();
        hotel.reservationService.subscribe(listener);
    }

    @Test
    void successfulBookingNotifiesOnceWithTheConfirmedReservation() throws BookingException {
        Reservation reservation = hotel.reservationService.book(
                "G003", "102", stay("2026-12-10", "2026-12-12"), 2);

        assertEquals(1, listener.bookedCalls);
        assertEquals(0, listener.cancelledCalls);
        assertSame(reservation, listener.lastBooked);
        assertEquals(ReservationStatus.CONFIRMED, listener.bookedStatus);
        assertSame(reservation, hotel.reservations.findById("R0001").orElseThrow());
    }

    @Test
    void successfulCancellationNotifiesOnceAfterTheStatusChanges() throws BookingException {
        Reservation reservation = hotel.reservationService.book(
                "G003", "102", stay("2026-12-10", "2026-12-12"), 2);

        hotel.reservationService.cancel(reservation.getId());

        assertEquals(1, listener.bookedCalls);
        assertEquals(1, listener.cancelledCalls);
        assertSame(reservation, listener.lastCancelled);
        assertEquals(ReservationStatus.CANCELLED, listener.cancelledStatus);
    }

    @Test
    void refusedOverlappingBookingDoesNotProduceAnotherEvent() throws BookingException {
        hotel.reservationService.book("G003", "102", stay("2026-12-10", "2026-12-12"), 2);

        assertThrows(RoomUnavailableException.class, () -> hotel.reservationService.book(
                "G001", "102", stay("2026-12-11", "2026-12-13"), 2));

        assertEquals(1, listener.bookedCalls);
        assertEquals(0, listener.cancelledCalls);
    }

    @Test
    void invalidBookingsNeverNotifyListeners() {
        assertThrows(CapacityExceededException.class, () -> hotel.reservationService.book(
                "G003", "102", stay("2026-12-10", "2026-12-12"), 3));
        assertThrows(InvalidDateRangeException.class, () -> hotel.reservationService.book(
                "G003", "102", stay("2026-11-10", "2026-11-12"), 1));
        assertThrows(GuestNotFoundException.class, () -> hotel.reservationService.book(
                "G999", "102", stay("2026-12-10", "2026-12-12"), 1));
        assertThrows(RoomNotFoundException.class, () -> hotel.reservationService.book(
                "G003", "999", stay("2026-12-10", "2026-12-12"), 1));

        assertEquals(0, listener.bookedCalls);
        assertEquals(0, listener.cancelledCalls);
    }

    @Test
    void repeatedOrUnknownCancellationDoesNotProduceAnotherEvent() throws BookingException {
        Reservation reservation = hotel.reservationService.book(
                "G003", "102", stay("2026-12-10", "2026-12-12"), 2);
        hotel.reservationService.cancel(reservation.getId());

        assertThrows(IllegalStateException.class,
                () -> hotel.reservationService.cancel(reservation.getId()));
        assertThrows(ReservationNotFoundException.class,
                () -> hotel.reservationService.cancel("R9999"));

        assertEquals(1, listener.bookedCalls);
        assertEquals(1, listener.cancelledCalls);
    }

    @Test
    void importingAnExistingReservationDoesNotNotifyListeners() throws BookingException {
        hotel.reservationService.importExisting(
                "G003", "102", stay("2026-11-10", "2026-11-12"), 2);

        assertEquals(1, hotel.reservations.count());
        assertEquals(0, listener.bookedCalls);
        assertEquals(0, listener.cancelledCalls);
    }

    @Test
    void csvImportStaysSilentForBothAcceptedAndRejectedReservations() throws DataLoadingException {
        DataImportService importer = new DataImportService(new CsvHotelDataSource(),
                hotel.rooms, hotel.guests, hotel.reservationService);

        ImportResult result = importer.importAll();

        assertEquals(4, result.reservations());
        assertEquals(1, result.rejectedReservations().size());
        assertEquals(0, listener.bookedCalls);
        assertEquals(0, listener.cancelledCalls);
    }

    @Test
    void unsubscribedListenerStopsReceivingEventsWhileOtherListenersContinue() throws BookingException {
        CountingListener otherListener = new CountingListener();
        hotel.reservationService.subscribe(otherListener);
        Reservation reservation = hotel.reservationService.book(
                "G003", "102", stay("2026-12-10", "2026-12-12"), 2);

        hotel.reservationService.unsubscribe(listener);
        hotel.reservationService.cancel(reservation.getId());

        assertEquals(1, listener.bookedCalls);
        assertEquals(0, listener.cancelledCalls);
        assertEquals(1, otherListener.bookedCalls);
        assertEquals(1, otherListener.cancelledCalls);
    }

    @Test
    void subscribingTheSameListenerTwiceDoesNotDuplicateEvents() throws BookingException {
        hotel.reservationService.subscribe(listener);

        hotel.reservationService.book("G003", "102", stay("2026-12-10", "2026-12-12"), 2);

        assertEquals(1, listener.bookedCalls);
    }

    @Test
    void missingListenerIsRefusedAtSubscription() {
        assertThrows(IllegalArgumentException.class, () -> hotel.reservationService.subscribe(null));
    }

    @Test
    void historyRecordsBookingAndCancellationInChronologicalOrder() throws BookingException {
        ReservationHistory history = new ReservationHistory();
        hotel.reservationService.subscribe(history);
        Reservation reservation = hotel.reservationService.book(
                "G003", "102", stay("2026-12-10", "2026-12-12"), 2);

        hotel.reservationService.cancel(reservation.getId());

        assertEquals(List.of("BOOKED R0001 | Chloe | room 102", "CANCELLED R0001 | Chloe | room 102"),
                history.getEvents());
    }

    @Test
    void historyReturnsAnUnmodifiableSnapshot() throws BookingException {
        ReservationHistory history = new ReservationHistory();
        hotel.reservationService.subscribe(history);
        Reservation reservation = hotel.reservationService.book(
                "G003", "102", stay("2026-12-10", "2026-12-12"), 2);
        List<String> snapshot = history.getEvents();

        hotel.reservationService.cancel(reservation.getId());

        assertEquals(List.of("BOOKED R0001 | Chloe | room 102"), snapshot);
        assertEquals(2, history.getEvents().size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("Unexpected event"));
    }

    @Test
    void listenerCanUnsubscribeAnotherListenerDuringNotification() throws BookingException {
        hotel.reservationService.unsubscribe(listener);
        ReservationListener removingListener = new ReservationListener() {
            @Override
            public void onBooked(Reservation reservation) {
                hotel.reservationService.unsubscribe(listener);
            }

            @Override
            public void onCancelled(Reservation reservation) {
                hotel.reservationService.unsubscribe(listener);
            }
        };
        hotel.reservationService.subscribe(removingListener);
        hotel.reservationService.subscribe(listener);

        Reservation reservation = hotel.reservationService.book(
                "G003", "102", stay("2026-12-10", "2026-12-12"), 2);
        hotel.reservationService.cancel(reservation.getId());

        assertEquals(1, listener.bookedCalls);
        assertEquals(0, listener.cancelledCalls);
    }

    private static class CountingListener implements ReservationListener {
        private int bookedCalls;
        private int cancelledCalls;
        private Reservation lastBooked;
        private Reservation lastCancelled;
        private ReservationStatus bookedStatus;
        private ReservationStatus cancelledStatus;

        @Override
        public void onBooked(Reservation reservation) {
            bookedCalls++;
            lastBooked = reservation;
            bookedStatus = reservation.getStatus();
        }

        @Override
        public void onCancelled(Reservation reservation) {
            cancelledCalls++;
            lastCancelled = reservation;
            cancelledStatus = reservation.getStatus();
        }
    }
}
