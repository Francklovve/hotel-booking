package com.junia.hotel.service;

import com.junia.hotel.model.DateRange;
import com.junia.hotel.model.Guest;
import com.junia.hotel.model.Reservation;
import com.junia.hotel.model.room.Room;
import com.junia.hotel.model.room.RoomType;
import com.junia.hotel.repository.Repository;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Management indicators computed from active reservations. */
public class ReportService {

    private final Repository<Room, String> rooms;
    private final Repository<Reservation, String> reservations;

    public ReportService(Repository<Room, String> rooms, Repository<Reservation, String> reservations) {
        this.rooms = rooms;
        this.reservations = reservations;
    }

    /** Revenue and best customer cover all active reservations; occupancy covers the given period only. */
    public Report buildReport(DateRange period) {
        if (period == null) {
            throw new IllegalArgumentException("Report period is required");
        }
        return new Report(period, revenueByRoomType(), totalRevenue(), occupancyRate(period),
                bestCustomer().map(Guest::getName).orElse("None"));
    }

    /** Booked room-nights divided by available room-nights over the period (0.0 to 1.0). */
    public double occupancyRate(DateRange period) {
        long availableRoomNights = rooms.count() * period.nights();
        if (availableRoomNights == 0) {
            return 0.0;
        }
        long bookedRoomNights = reservations.findAll().stream()
                .filter(Reservation::isActive)
                .mapToLong(reservation -> reservation.getStay().nightDates()
                        .filter(period::contains)
                        .count())
                .sum();
        return (double) bookedRoomNights / availableRoomNights;
    }

    public Map<RoomType, Double> revenueByRoomType() {
        return reservations.findAll().stream()
                .filter(Reservation::isActive)
                .collect(Collectors.groupingBy(
                        reservation -> reservation.getRoom().getType(),
                        () -> new EnumMap<>(RoomType.class),
                        Collectors.summingDouble(Reservation::getTotalPrice)));
    }

    public double totalRevenue() {
        return reservations.findAll().stream()
                .filter(Reservation::isActive)
                .mapToDouble(Reservation::getTotalPrice)
                .sum();
    }

    /** Guest who spent the most on active reservations, if any. */
    public Optional<Guest> bestCustomer() {
        return reservations.findAll().stream()
                .filter(Reservation::isActive)
                .collect(Collectors.groupingBy(Reservation::getGuest,
                        Collectors.summingDouble(Reservation::getTotalPrice)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }
}
