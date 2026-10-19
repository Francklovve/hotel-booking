package com.junia.hotel.exception;

public class GuestNotFoundException extends BookingException {

    public GuestNotFoundException(String id) {
        super("No guest with id " + id);
    }
}
