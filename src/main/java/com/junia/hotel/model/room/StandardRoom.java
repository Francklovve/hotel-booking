package com.junia.hotel.model.room;

import java.time.LocalDate;

/** Simplest room: flat price, whatever the day or the number of guests. */
public class StandardRoom extends Room {

    public StandardRoom(String number, int capacity, double basePricePerNight) {
        super(number, capacity, basePricePerNight);
    }

    @Override
    public RoomType getType() {
        return RoomType.STANDARD;
    }

    @Override
    protected double priceForNight(LocalDate night, int guestCount) {
        return getBasePricePerNight();
    }
}
