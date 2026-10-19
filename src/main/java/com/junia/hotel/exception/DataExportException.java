package com.junia.hotel.exception;

/** Technical export failure; the original cause remains available for diagnosis. */
public class DataExportException extends Exception {

    public DataExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
