package com.junia.hotel.repository;

import com.junia.hotel.model.room.Room;
import com.junia.hotel.model.room.StandardRoom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRepositoryTest {

    private final Repository<Room, String> repository = new InMemoryRepository<>(Room::getNumber);

    @Test
    void findByIdReturnsEmptyOptionalWhenAbsent() {
        assertTrue(repository.findById("404").isEmpty());
    }

    @Test
    void savingWithAnExistingIdReplacesTheItem() {
        repository.save(new StandardRoom("101", 1, 65));
        repository.save(new StandardRoom("101", 2, 90));

        assertEquals(1, repository.count());
        assertEquals(90, repository.findById("101").orElseThrow().getBasePricePerNight(), 0.001);
    }

    @Test
    void findAllKeepsInsertionOrder() {
        repository.save(new StandardRoom("102", 1, 65));
        repository.save(new StandardRoom("101", 1, 65));

        assertEquals("102", repository.findAll().get(0).getNumber());
    }
}
