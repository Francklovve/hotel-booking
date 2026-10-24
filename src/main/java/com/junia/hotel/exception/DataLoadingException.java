package com.junia.hotel.exception;

/** Erreur de lecture des données, avec la cause d'origine. */
public class DataLoadingException extends Exception {

    public DataLoadingException(String message) {
        super(message);
    }

    public DataLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
