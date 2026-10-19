package com.junia.hotel;

import com.junia.hotel.model.DateRange;

import java.time.LocalDate;

/** Small helpers shared by tests to keep the Arrange step readable. */
public final class TestData {

    private TestData() {
    }

    public static DateRange stay(String checkIn, String checkOut) {
        return new DateRange(LocalDate.parse(checkIn), LocalDate.parse(checkOut));
    }
}
