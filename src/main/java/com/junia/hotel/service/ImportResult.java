package com.junia.hotel.service;

import java.util.List;

/** Outcome of the startup import: how many objects were loaded and which lines were refused. */
public record ImportResult(int rooms, int guests, int reservations, List<String> rejectedReservations) {

    public ImportResult {
        rejectedReservations = List.copyOf(rejectedReservations);
    }
}
