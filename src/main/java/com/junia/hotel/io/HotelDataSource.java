package com.junia.hotel.io;

import com.junia.hotel.exception.DataLoadingException;
import com.junia.hotel.model.Guest;
import com.junia.hotel.model.room.Room;

import java.util.List;

/** Where the initial hotel data comes from. CSV today; JSON or a database could implement it later. */
public interface HotelDataSource {

    List<Room> loadRooms() throws DataLoadingException;

    List<Guest> loadGuests() throws DataLoadingException;

    List<ReservationRecord> loadReservations() throws DataLoadingException;
}
