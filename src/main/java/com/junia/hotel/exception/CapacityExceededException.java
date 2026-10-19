package com.junia.hotel.exception;

public class CapacityExceededException extends BookingException {

    public CapacityExceededException(String roomNumber, int capacity, int requested) {
        super("Room " + roomNumber + " accepts " + capacity + " guest(s), " + requested + " requested");
    }
}
