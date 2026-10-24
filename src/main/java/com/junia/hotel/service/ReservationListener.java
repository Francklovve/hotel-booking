package com.junia.hotel.service;

import com.junia.hotel.model.Reservation;

/** Reçoit les réservations et les annulations réussies pendant la session. */
public interface ReservationListener {

    void onBooked(Reservation reservation);

    void onCancelled(Reservation reservation);
}
