package com.junia.hotel.service;

import com.junia.hotel.model.DateRange;
import com.junia.hotel.model.Reservation;
import com.junia.hotel.model.room.Room;
import com.junia.hotel.repository.Repository;

import java.util.Comparator;
import java.util.List;

/** Vérifie les conflits avec les réservations actives. */
public class AvailabilityService {

    private final Repository<Room, String> rooms;
    private final Repository<Reservation, String> reservations;

    public AvailabilityService(Repository<Room, String> rooms, Repository<Reservation, String> reservations) {
        this.rooms = rooms;
        this.reservations = reservations;
    }

    public boolean isAvailable(Room room, DateRange stay) {
        return reservations.findAll().stream()
                .noneMatch(reservation -> reservation.blocks(room, stay));
    }

    /** Chambres libres et assez grandes pour le séjour, triées par prix de base. */
    public List<Room> findAvailableRooms(DateRange stay, int guestCount) {
        return rooms.findAll().stream()
                .filter(room -> room.canAccommodate(guestCount))
                .filter(room -> isAvailable(room, stay))
                .sorted(Comparator.comparingDouble(Room::getBasePricePerNight))
                .toList();
    }
}
