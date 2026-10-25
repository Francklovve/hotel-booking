package com.junia.hotel.exception;

public class ReservationNotFoundException extends BookingException {

    public ReservationNotFoundException(String id) {
        super("No reservation with id " + id);
    }
}
