package com.junia.hotel;

import com.junia.hotel.model.DateRange;

import java.time.LocalDate;

public final class TestData {

    private TestData() {
    }

    public static DateRange stay(String checkIn, String checkOut) {
        return new DateRange(LocalDate.parse(checkIn), LocalDate.parse(checkOut));
    }
}
