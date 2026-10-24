package com.junia.hotel.ui;

import com.junia.hotel.model.Reservation;
import com.junia.hotel.service.ReservationListener;

import java.io.PrintStream;

/** Affiche une confirmation pour simuler un e-mail, sans en envoyer. */
public class ConsoleNotifier implements ReservationListener {

    private final PrintStream out;

    public ConsoleNotifier(PrintStream out) {
        this.out = out;
    }

    @Override
    public void onBooked(Reservation reservation) {
        out.printf("  Confirmation email to %s: reservation %s booked.%n",
                reservation.getGuest().getEmail(), reservation.getId());
    }

    @Override
    public void onCancelled(Reservation reservation) {
        out.printf("  Confirmation email to %s: reservation %s cancelled.%n",
                reservation.getGuest().getEmail(), reservation.getId());
    }
}
