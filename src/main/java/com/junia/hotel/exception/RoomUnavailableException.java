package com.junia.hotel.exception;

import com.junia.hotel.model.DateRange;

public class RoomUnavailableException extends BookingException {

    public RoomUnavailableException(String roomNumber, DateRange stay) {
        super("Room " + roomNumber + " is already booked during " + stay);
    }
}
