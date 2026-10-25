package com.junia.hotel.model;

import com.junia.hotel.exception.InvalidDateRangeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.junia.hotel.TestData.stay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateRangeTest {

    @Test
    void countsNightsBetweenCheckInAndCheckOut() {
        assertEquals(3, stay("2026-12-24", "2026-12-27").nights());
    }

    @Test
    void stayAcrossNewYearCountsNightsCorrectly() {
        assertEquals(2, stay("2026-12-31", "2027-01-02").nights());
    }

    @Test
    void overlappingStaysAreDetected() {
        assertTrue(stay("2026-12-24", "2026-12-27").overlaps(stay("2026-12-26", "2026-12-28")));
    }

    @Test
    void stayInsideAnotherStayOverlaps() {
        assertTrue(stay("2026-12-20", "2026-12-30").overlaps(stay("2026-12-24", "2026-12-25")));
    }

    @Test
    void checkOutDayOfOneStayCanBeCheckInDayOfTheNext() {
        assertFalse(stay("2026-12-24", "2026-12-27").overlaps(stay("2026-12-27", "2026-12-29")));
    }

    @Test
    void checkOutBeforeCheckInIsRefused() {
        assertThrows(InvalidDateRangeException.class, () -> stay("2026-12-27", "2026-12-24"));
    }

    @Test
    void zeroNightStayIsRefused() {
        InvalidDateRangeException exception =
                assertThrows(InvalidDateRangeException.class, () -> stay("2026-12-24", "2026-12-24"));
        assertTrue(exception.getMessage().contains("must be after"));
    }

    @Test
    void missingDateIsRefused() {
        assertThrows(NullPointerException.class, () -> new DateRange(null, LocalDate.of(2026, 12, 24)));
    }
}
