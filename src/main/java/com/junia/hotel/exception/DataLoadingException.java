package com.junia.hotel.exception;

/** Technical error while reading the data files. Keeps the original cause (e.g. IOException). */
public class DataLoadingException extends Exception {

    public DataLoadingException(String message) {
        super(message);
    }

    public DataLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
