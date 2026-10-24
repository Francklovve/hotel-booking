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

/** Calcule les chiffres de l'hôtel à partir des réservations actives. */
public class ReportService {

    private final Repository<Room, String> rooms;
    private final Repository<Reservation, String> reservations;

    public ReportService(Repository<Room, String> rooms, Repository<Reservation, String> reservations) {
        this.rooms = rooms;
        this.reservations = reservations;
    }

    /** Les revenus et le meilleur client sont globaux ; seule l'occupation dépend de la période. */
    public Report buildReport(DateRange period) {
        if (period == null) {
            throw new IllegalArgumentException("Report period is required");
        }
        return new Report(period, revenueByRoomType(), totalRevenue(), occupancyRate(period),
                bestCustomer().map(Guest::getName).orElse("None"));
    }

    /** Nuits réservées divisées par les nuits disponibles sur la période, entre 0 et 1. */
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

    /** Client qui a le plus dépensé sur ses réservations actives, s'il y en a un. */
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
