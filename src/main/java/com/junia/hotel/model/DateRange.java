package com.junia.hotel.model;

import com.junia.hotel.exception.InvalidDateRangeException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Immutable period of a stay. The check-out day is excluded:
 * a stay from the 10th to the 12th is two nights (10th and 11th).
 */
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

    /** Each night of the stay, identified by the date the guest goes to sleep. */
    public Stream<LocalDate> nightDates() {
        return checkIn.datesUntil(checkOut);
    }

    /**
     * Half-open intervals: a guest leaving on the 12th does not conflict
     * with a guest arriving on the 12th.
     */
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
