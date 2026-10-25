package com.junia.hotel.exception;

public class RoomNotFoundException extends BookingException {

    public RoomNotFoundException(String id) {
        super("No room with number " + id);
    }
}
