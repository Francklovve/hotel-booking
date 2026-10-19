package com.junia.hotel.exception;

/**
 * Base of all business errors (checked): the caller is expected to handle them,
 * typically by explaining to the user why the operation was refused.
 */
public abstract class BookingException extends Exception {

    protected BookingException(String message) {
        super(message);
    }
}
