package com.junia.hotel.exception;

/** Erreur métier que le menu peut expliquer à l'utilisateur. */
public abstract class BookingException extends Exception {

    protected BookingException(String message) {
        super(message);
    }
}
