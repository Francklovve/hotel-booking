package com.junia.hotel.service;

import com.junia.hotel.model.Reservation;

import java.util.ArrayList;
import java.util.List;

/** Garde les opérations réussies dans leur ordre d'arrivée. */
public class ReservationHistory implements ReservationListener {

    private final List<String> events = new ArrayList<>();

    @Override
    public void onBooked(Reservation reservation) {
        events.add(describe("BOOKED", reservation));
    }

    @Override
    public void onCancelled(Reservation reservation) {
        events.add(describe("CANCELLED", reservation));
    }

    public List<String> getEvents() {
        return List.copyOf(events);
    }

    private String describe(String action, Reservation reservation) {
        return action + " " + reservation.getId() + " | " + reservation.getGuest().getName()
                + " | room " + reservation.getRoom().getNumber();
    }
}
