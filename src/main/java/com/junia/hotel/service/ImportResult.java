package com.junia.hotel.service;

import java.util.List;

public record ImportResult(int rooms, int guests, int reservations, List<String> rejectedReservations) {

    public ImportResult {
        rejectedReservations = List.copyOf(rejectedReservations);
    }
}
