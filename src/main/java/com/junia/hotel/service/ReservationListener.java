package com.junia.hotel.service;

import com.junia.hotel.model.Reservation;

public interface ReservationListener {

    void onBooked(Reservation reservation);

    void onCancelled(Reservation reservation);
}
