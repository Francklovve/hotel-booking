package com.junia.hotel.service;

import com.junia.hotel.model.Reservation;

/** Observer of successful reservation changes made by a user. */
public interface ReservationListener {

    void onBooked(Reservation reservation);

    void onCancelled(Reservation reservation);
}
