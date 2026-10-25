package com.junia.hotel.service;

import com.junia.hotel.exception.BookingException;
import com.junia.hotel.model.room.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.junia.hotel.TestData.stay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvailabilityServiceTest {

    private HotelFixture hotel;

    @BeforeEach
    void setUp() {
        hotel = new HotelFixture();
    }

    @Test
    void allRoomsAreAvailableInAnEmptyHotelCheapestFirst() {
        List<Room> rooms = hotel.availabilityService.findAvailableRooms(stay("2026-12-10", "2026-12-12"), 1);

        assertEquals(List.of("102", "301", "201"), rooms.stream().map(Room::getNumber).toList());
    }

    @Test
    void bookedRoomIsExcludedFromSearch() throws BookingException {
        hotel.reservationService.book("G003", "102", stay("2026-12-10", "2026-12-12"), 1);

        List<Room> rooms = hotel.availabilityService.findAvailableRooms(stay("2026-12-11", "2026-12-13"), 1);

        assertTrue(rooms.stream().noneMatch(room -> room.getNumber().equals("102")));
    }

    @Test
    void roomsTooSmallAreExcludedFromSearch() {
        List<Room> rooms = hotel.availabilityService.findAvailableRooms(stay("2026-12-10", "2026-12-12"), 3);

        assertEquals(List.of("301"), rooms.stream().map(Room::getNumber).toList());
    }

    @Test
    void noRoomForAGroupLargerThanEveryRoom() {
        assertTrue(hotel.availabilityService.findAvailableRooms(stay("2026-12-10", "2026-12-12"), 10).isEmpty());
    }
}
