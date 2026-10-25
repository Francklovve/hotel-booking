package com.junia.hotel.exception;

public class InvalidDateRangeException extends IllegalArgumentException {

    public InvalidDateRangeException(String message) {
        super(message);
    }
}
