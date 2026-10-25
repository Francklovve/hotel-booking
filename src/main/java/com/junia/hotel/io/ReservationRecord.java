package com.junia.hotel.io;

import com.junia.hotel.model.DateRange;

public record ReservationRecord(int lineNumber, String guestId, String roomNumber, DateRange stay, int guestCount) {
}
