package com.junia.hotel.io;

import com.junia.hotel.model.DateRange;

/**
 * Raw reservation read from the file. It is not a Reservation yet:
 * it still has to pass the business rules of the ReservationService.
 */
public record ReservationRecord(int lineNumber, String guestId, String roomNumber, DateRange stay, int guestCount) {
}
