package com.junia.hotel.model;

import com.junia.hotel.exception.InvalidDateRangeException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Stream;

public record DateRange(LocalDate checkIn, LocalDate checkOut) {

    public DateRange {
        Objects.requireNonNull(checkIn, "Check-in date is required");
        Objects.requireNonNull(checkOut, "Check-out date is required");
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDateRangeException(
                    "Check-out (" + checkOut + ") must be after check-in (" + checkIn + ")");
        }
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public Stream<LocalDate> nightDates() {
        return checkIn.datesUntil(checkOut);
    }

    public boolean overlaps(DateRange other) {
        return checkIn.isBefore(other.checkOut) && other.checkIn.isBefore(checkOut);
    }

    public boolean contains(LocalDate night) {
        return !night.isBefore(checkIn) && night.isBefore(checkOut);
    }

    @Override
    public String toString() {
        return checkIn + " -> " + checkOut + " (" + nights() + " night(s))";
    }
}
