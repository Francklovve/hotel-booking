package com.junia.hotel.exception;

public abstract class BookingException extends Exception {

    protected BookingException(String message) {
        super(message);
    }
}
