package com.junia.hotel.exception;

/** Erreur d'export, avec la cause d'origine. */
public class DataExportException extends Exception {

    public DataExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
