package com.junia.hotel.io;

import com.junia.hotel.model.DateRange;

/** Réservation lue dans un fichier, à vérifier avant de l'enregistrer. */
public record ReservationRecord(int lineNumber, String guestId, String roomNumber, DateRange stay, int guestCount) {
}
