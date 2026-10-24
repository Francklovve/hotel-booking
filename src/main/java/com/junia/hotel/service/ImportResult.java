package com.junia.hotel.service;

import java.util.List;

/** Nombre d'objets chargés et réservations refusées pendant l'import. */
public record ImportResult(int rooms, int guests, int reservations, List<String> rejectedReservations) {

    public ImportResult {
        rejectedReservations = List.copyOf(rejectedReservations);
    }
}
