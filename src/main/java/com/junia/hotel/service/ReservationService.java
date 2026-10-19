package com.junia.hotel.service;

import com.junia.hotel.exception.BookingException;
import com.junia.hotel.exception.CancellationNotAllowedException;
import com.junia.hotel.exception.CapacityExceededException;
import com.junia.hotel.exception.GuestNotFoundException;
import com.junia.hotel.exception.InvalidDateRangeException;
import com.junia.hotel.exception.ReservationNotFoundException;
import com.junia.hotel.exception.RoomNotFoundException;
import com.junia.hotel.exception.RoomUnavailableException;
import com.junia.hotel.model.DateRange;
import com.junia.hotel.model.Guest;
import com.junia.hotel.model.Reservation;
import com.junia.hotel.model.room.Room;
import com.junia.hotel.pricing.CancellationPolicy;
import com.junia.hotel.pricing.CancellationPolicyResolver;
import com.junia.hotel.pricing.PriceQuote;
import com.junia.hotel.pricing.PricingService;
import com.junia.hotel.repository.Repository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Booking and cancellation workflow: validates business rules, then records the reservation. */
public class ReservationService {

    private final Repository<Room, String> rooms;
    private final Repository<Guest, String> guests;
    private final Repository<Reservation, String> reservations;
    private final AvailabilityService availabilityService;
    private final PricingService pricingService;
    private final CancellationPolicyResolver cancellationPolicyResolver;
    // Injected instead of calling LocalDate.now() directly so tests can fix "today".
    private final Clock clock;
    private final List<ReservationListener> listeners = new ArrayList<>();

    public ReservationService(Repository<Room, String> rooms,
                              Repository<Guest, String> guests,
                              Repository<Reservation, String> reservations,
                              AvailabilityService availabilityService,
                              PricingService pricingService,
                              CancellationPolicyResolver cancellationPolicyResolver,
                              Clock clock) {
        this.rooms = rooms;
        this.guests = guests;
        this.reservations = reservations;
        this.availabilityService = availabilityService;
        this.pricingService = pricingService;
        this.cancellationPolicyResolver = cancellationPolicyResolver;
        this.clock = clock;
    }

    /** New booking made today: a stay cannot start in the past. */
    public Reservation book(String guestId, String roomNumber, DateRange stay, int guestCount)
            throws BookingException {
        if (stay.checkIn().isBefore(LocalDate.now(clock))) {
            throw new InvalidDateRangeException("Check-in date " + stay.checkIn() + " is in the past");
        }
        Reservation reservation = createReservation(guestId, roomNumber, stay, guestCount);
        // A snapshot lets an observer unsubscribe during its callback.
        for (ReservationListener listener : List.copyOf(listeners)) {
            listener.onBooked(reservation);
        }
        return reservation;
    }

    /**
     * Existing booking loaded from the data file: it may be in the past (history),
     * but it must still respect capacity and must not overlap another booking.
     */
    public Reservation importExisting(String guestId, String roomNumber, DateRange stay, int guestCount)
            throws BookingException {
        return createReservation(guestId, roomNumber, stay, guestCount);
    }

    public PriceQuote quote(String guestId, String roomNumber, DateRange stay, int guestCount)
            throws BookingException {
        Guest guest = findGuest(guestId);
        Room room = findRoom(roomNumber);
        checkCapacity(room, guestCount);
        return pricingService.quote(room, guest, stay, guestCount);
    }

    /**
     * Cancels a reservation that has not started yet; the check-in day itself is still allowed
     * (late cancellation, charged by the policy). The fee is computed on the frozen final price.
     */
    public CancellationResult cancel(String reservationId)
            throws ReservationNotFoundException, CancellationNotAllowedException {
        Reservation reservation = reservations.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        LocalDate today = LocalDate.now(clock);
        if (today.isAfter(reservation.getStay().checkIn())) {
            throw new CancellationNotAllowedException(reservationId, reservation.getStay().checkIn());
        }
        CancellationPolicy policy = cancellationPolicyResolver.resolve(reservation.getRoom().getType());
        double rate = policy.feeRate(reservation.getStay(), today);
        double fee = Math.round(reservation.getTotalPrice() * rate * 100) / 100.0;
        double refund = Math.round((reservation.getTotalPrice() - fee) * 100) / 100.0;
        reservation.cancel();
        for (ReservationListener listener : List.copyOf(listeners)) {
            listener.onCancelled(reservation);
        }
        return new CancellationResult(reservation, fee, refund);
    }

    public void subscribe(ReservationListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Reservation listener is required");
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(ReservationListener listener) {
        listeners.remove(listener);
    }

    public List<Reservation> findByGuest(String guestId) throws GuestNotFoundException {
        Guest guest = findGuest(guestId);
        return reservations.findAll().stream()
                .filter(reservation -> reservation.getGuest().equals(guest))
                .toList();
    }

    public List<Reservation> findAll() {
        return reservations.findAll();
    }

    private Reservation createReservation(String guestId, String roomNumber, DateRange stay, int guestCount)
            throws BookingException {
        Guest guest = findGuest(guestId);
        Room room = findRoom(roomNumber);
        checkCapacity(room, guestCount);
        if (!availabilityService.isAvailable(room, stay)) {
            throw new RoomUnavailableException(roomNumber, stay);
        }
        PriceQuote quote = pricingService.quote(room, guest, stay, guestCount);
        Reservation reservation = new Reservation(nextId(), guest, room, stay, guestCount, quote);
        reservations.save(reservation);
        return reservation;
    }

    private Guest findGuest(String guestId) throws GuestNotFoundException {
        return guests.findById(guestId).orElseThrow(() -> new GuestNotFoundException(guestId));
    }

    private Room findRoom(String roomNumber) throws RoomNotFoundException {
        return rooms.findById(roomNumber).orElseThrow(() -> new RoomNotFoundException(roomNumber));
    }

    private void checkCapacity(Room room, int guestCount) throws CapacityExceededException {
        if (guestCount <= 0) {
            throw new IllegalArgumentException("Guest count must be positive");
        }
        if (!room.canAccommodate(guestCount)) {
            throw new CapacityExceededException(room.getNumber(), room.getCapacity(), guestCount);
        }
    }

    private String nextId() {
        // Reservations are never deleted (only cancelled), so the count gives unique ids.
        return String.format("R%04d", reservations.count() + 1);
    }
}
