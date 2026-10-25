package com.junia.hotel.exception;

import java.time.LocalDate;

public class CancellationNotAllowedException extends BookingException {

    public CancellationNotAllowedException(String reservationId, LocalDate checkIn) {
        super("Reservation " + reservationId + " cannot be cancelled: the stay started on " + checkIn);
    }
}
