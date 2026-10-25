package com.junia.hotel.model.room;

import com.junia.hotel.model.DateRange;

import java.time.LocalDate;
import java.util.Objects;

public abstract class Room {

    private final String number;
    private final int capacity;
    private final double basePricePerNight;

    protected Room(String number, int capacity, double basePricePerNight) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Room number is required");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Room capacity must be positive (room " + number + ")");
        }
        if (basePricePerNight <= 0) {
            throw new IllegalArgumentException("Base price must be positive (room " + number + ")");
        }
        this.number = number;
        this.capacity = capacity;
        this.basePricePerNight = basePricePerNight;
    }

    public abstract RoomType getType();

    protected abstract double priceForNight(LocalDate night, int guestCount);

    public final double priceForStay(DateRange stay, int guestCount) {
        if (guestCount <= 0) {
            throw new IllegalArgumentException("Guest count must be positive");
        }
        return stay.nightDates()
                .mapToDouble(night -> priceForNight(night, guestCount))
                .sum();
    }

    public boolean canAccommodate(int guestCount) {
        return guestCount > 0 && guestCount <= capacity;
    }

    public String getNumber() {
        return number;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getBasePricePerNight() {
        return basePricePerNight;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Room other)) {
            return false;
        }
        return number.equals(other.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return String.format("Room %s [%s] capacity %d, from %.2f EUR/night",
                number, getType(), capacity, basePricePerNight);
    }
}
