package com.junia.hotel.exception;

/**
 * Unchecked: an invalid period is an invalid argument (like IllegalArgumentException),
 * it should be prevented by validating input rather than recovered from.
 */
public class InvalidDateRangeException extends IllegalArgumentException {

    public InvalidDateRangeException(String message) {
        super(message);
    }
}
