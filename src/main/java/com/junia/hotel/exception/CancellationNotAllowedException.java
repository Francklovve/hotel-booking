package com.junia.hotel.exception;

import java.time.LocalDate;

/** L'annulation est refusée après le jour d'arrivée. */
public class CancellationNotAllowedException extends BookingException {

    public CancellationNotAllowedException(String reservationId, LocalDate checkIn) {
        super("Reservation " + reservationId + " cannot be cancelled: the stay started on " + checkIn);
    }
}
