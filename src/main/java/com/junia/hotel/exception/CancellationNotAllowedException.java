package com.junia.hotel.exception;

import java.time.LocalDate;

/** A stay that has already started (or is over) can no longer be cancelled. */
public class CancellationNotAllowedException extends BookingException {

    public CancellationNotAllowedException(String reservationId, LocalDate checkIn) {
        super("Reservation " + reservationId + " cannot be cancelled: the stay started on " + checkIn);
    }
}
