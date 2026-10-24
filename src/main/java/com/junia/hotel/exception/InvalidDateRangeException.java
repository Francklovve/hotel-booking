package com.junia.hotel.exception;

/** Dates incompatibles avec un séjour ou une nouvelle réservation. */
public class InvalidDateRangeException extends IllegalArgumentException {

    public InvalidDateRangeException(String message) {
        super(message);
    }
}
