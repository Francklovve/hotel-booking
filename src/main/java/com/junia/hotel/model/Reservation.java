package com.junia.hotel.model;

import com.junia.hotel.model.room.Room;
import com.junia.hotel.pricing.PriceQuote;

import java.util.Objects;

/** Réservation d'une chambre pour un client. Le prix du devis est gardé au moment de la réservation. */
public class Reservation {

    private final String id;
    private final Guest guest;
    private final Room room;
    private final DateRange stay;
    private final int guestCount;
    private final PriceQuote quote;
    private ReservationStatus status;

    public Reservation(String id, Guest guest, Room room, DateRange stay, int guestCount, PriceQuote quote) {
        this.id = Objects.requireNonNull(id);
        this.guest = Objects.requireNonNull(guest);
        this.room = Objects.requireNonNull(room);
        this.stay = Objects.requireNonNull(stay);
        this.quote = Objects.requireNonNull(quote);
        if (guestCount <= 0) {
            throw new IllegalArgumentException("A reservation needs at least one guest");
        }
        this.guestCount = guestCount;
        this.status = ReservationStatus.CONFIRMED;
    }

    /** Seule une réservation confirmée peut être annulée. */
    public void cancel() {
        if (status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation " + id + " is already cancelled");
        }
        status = ReservationStatus.CANCELLED;
    }

    public boolean isActive() {
        return status == ReservationStatus.CONFIRMED;
    }

    /** Indique si cette réservation bloque la chambre pendant la période demandée. */
    public boolean blocks(Room otherRoom, DateRange period) {
        return isActive() && room.equals(otherRoom) && stay.overlaps(period);
    }

    public String getId() {
        return id;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public DateRange getStay() {
        return stay;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public PriceQuote getQuote() {
        return quote;
    }

    public double getTotalPrice() {
        return quote.total();
    }

    public ReservationStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | room %s | %s | %d guest(s) | %.2f EUR | %s",
                id, guest.getName(), room.getNumber(), stay, guestCount, getTotalPrice(), status);
    }
}
