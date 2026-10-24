package com.junia.hotel.io;

import com.junia.hotel.exception.DataLoadingException;
import com.junia.hotel.model.Guest;
import com.junia.hotel.model.room.Room;

import java.util.List;

/** Charge les chambres, les clients et les réservations de départ. */
public interface HotelDataSource {

    List<Room> loadRooms() throws DataLoadingException;

    List<Guest> loadGuests() throws DataLoadingException;

    List<ReservationRecord> loadReservations() throws DataLoadingException;
}
