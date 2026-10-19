package com.junia.hotel.service;

import com.junia.hotel.exception.BookingException;
import com.junia.hotel.exception.DataLoadingException;
import com.junia.hotel.io.HotelDataSource;
import com.junia.hotel.io.ReservationRecord;
import com.junia.hotel.model.Guest;
import com.junia.hotel.model.room.Room;
import com.junia.hotel.repository.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Fills the repositories from a data source at startup.
 * Existing reservations go through the ReservationService so that a conflicting
 * line in the file is refused exactly like a conflicting booking made by a user.
 */
public class DataImportService {

    private final HotelDataSource dataSource;
    private final Repository<Room, String> rooms;
    private final Repository<Guest, String> guests;
    private final ReservationService reservationService;

    public DataImportService(HotelDataSource dataSource,
                             Repository<Room, String> rooms,
                             Repository<Guest, String> guests,
                             ReservationService reservationService) {
        this.dataSource = dataSource;
        this.rooms = rooms;
        this.guests = guests;
        this.reservationService = reservationService;
    }

    public ImportResult importAll() throws DataLoadingException {
        List<Room> loadedRooms = dataSource.loadRooms();
        loadedRooms.forEach(rooms::save);

        List<Guest> loadedGuests = dataSource.loadGuests();
        loadedGuests.forEach(guests::save);

        int imported = 0;
        List<String> rejected = new ArrayList<>();
        for (ReservationRecord record : dataSource.loadReservations()) {
            try {
                reservationService.importExisting(record.guestId(), record.roomNumber(),
                        record.stay(), record.guestCount());
                imported++;
            } catch (BookingException e) {
                // One bad booking must not prevent the hotel from starting: report and continue.
                rejected.add("reservations.csv line " + record.lineNumber() + ": " + e.getMessage());
            }
        }
        return new ImportResult(loadedRooms.size(), loadedGuests.size(), imported, rejected);
    }
}
