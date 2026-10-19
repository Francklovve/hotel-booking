package com.junia.hotel.model.room;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomFactoryTest {

    @Test
    void createsTheConcreteClassMatchingTheType() {
        assertInstanceOf(StandardRoom.class, RoomFactory.create(RoomType.STANDARD, "101", 1, 65, ""));
        assertInstanceOf(Suite.class, RoomFactory.create(RoomType.SUITE, "201", 2, 200, "true"));
        assertInstanceOf(FamilyRoom.class, RoomFactory.create(RoomType.FAMILY, "301", 4, 120, "25"));
    }

    @Test
    void suiteOptionMeansBreakfastIncluded() {
        Suite suite = (Suite) RoomFactory.create(RoomType.SUITE, "201", 2, 200, "true");

        assertTrue(suite.isBreakfastIncluded());
    }

    @Test
    void familyOptionIsTheExtraGuestFee() {
        FamilyRoom room = (FamilyRoom) RoomFactory.create(RoomType.FAMILY, "301", 4, 120, "25");

        assertEquals(25, room.getExtraGuestFee(), 0.001);
    }

    @Test
    void familyRoomWithoutFeeIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> RoomFactory.create(RoomType.FAMILY, "301", 4, 120, ""));
    }
}
