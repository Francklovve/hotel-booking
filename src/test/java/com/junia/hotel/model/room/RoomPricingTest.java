package com.junia.hotel.model.room;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.junia.hotel.TestData.stay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 2026-12-14 is a Monday, 2026-12-18 a Friday, 2026-12-19 a Saturday. */
class RoomPricingTest {

    @Test
    void standardRoomPriceIsBasePriceTimesNights() {
        Room room = new StandardRoom("102", 2, 80);

        assertEquals(240, room.priceForStay(stay("2026-12-14", "2026-12-17"), 2), 0.001);
    }

    @Test
    void suiteWeekdayNightCostsBasePrice() {
        Room suite = new Suite("202", 2, 200, false);

        assertEquals(200, suite.priceForStay(stay("2026-12-14", "2026-12-15"), 2), 0.001);
    }

    @Test
    void suiteFridayAndSaturdayNightsHaveWeekendSurcharge() {
        Room suite = new Suite("202", 2, 200, false);

        // Thursday 200 + Friday 250 + Saturday 250
        assertEquals(700, suite.priceForStay(stay("2026-12-17", "2026-12-20"), 2), 0.001);
    }

    @Test
    void suiteWithBreakfastAddsBreakfastPerGuestAndPerNight() {
        Room suite = new Suite("201", 2, 200, true);

        // 2 weekday nights * (200 + 2 * 15)
        assertEquals(460, suite.priceForStay(stay("2026-12-14", "2026-12-16"), 2), 0.001);
    }

    @Test
    void familyRoomIncludesTwoGuests() {
        Room room = new FamilyRoom("301", 4, 120, 25);

        assertEquals(120, room.priceForStay(stay("2026-12-14", "2026-12-15"), 2), 0.001);
    }

    @Test
    void familyRoomChargesEachExtraGuest() {
        Room room = new FamilyRoom("301", 4, 120, 25);

        // 2 nights * (120 + 2 extra guests * 25)
        assertEquals(340, room.priceForStay(stay("2026-12-14", "2026-12-16"), 4), 0.001);
    }

    @Test
    void sameStayHasDifferentPricesDependingOnTheActualRoomType() {
        List<Room> rooms = List.of(
                new StandardRoom("102", 3, 100),
                new Suite("202", 3, 100, false),
                new FamilyRoom("301", 3, 100, 10));

        List<Double> prices = rooms.stream()
                .map(room -> room.priceForStay(stay("2026-12-18", "2026-12-19"), 3))
                .toList();

        assertEquals(List.of(100.0, 125.0, 110.0), prices);
    }

    @Test
    void capacityIsRespectedUpToTheLimit() {
        Room room = new StandardRoom("102", 2, 80);

        assertTrue(room.canAccommodate(2));
        assertFalse(room.canAccommodate(3));
        assertFalse(room.canAccommodate(0));
    }

    @Test
    void roomWithNegativePriceCannotBeCreated() {
        assertThrows(IllegalArgumentException.class, () -> new StandardRoom("102", 2, -10));
    }

    @Test
    void priceForZeroGuestIsRefused() {
        Room room = new StandardRoom("102", 2, 80);

        assertThrows(IllegalArgumentException.class, () -> room.priceForStay(stay("2026-12-14", "2026-12-15"), 0));
    }
}
