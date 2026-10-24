package com.junia.hotel.io;

import com.junia.hotel.exception.DataLoadingException;
import com.junia.hotel.model.DateRange;
import com.junia.hotel.model.Guest;
import com.junia.hotel.model.LoyaltyTier;
import com.junia.hotel.model.room.Room;
import com.junia.hotel.model.room.RoomFactory;
import com.junia.hotel.model.room.RoomType;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Convertit les lignes CSV en objets. En cas d'erreur, indique le fichier et la ligne concernés. */
public class CsvHotelDataSource implements HotelDataSource {

    public static final String ROOMS_FILE = "rooms.csv";
    public static final String GUESTS_FILE = "guests.csv";
    public static final String RESERVATIONS_FILE = "reservations.csv";

    private final CsvFileReader reader;

    /** Charge les fichiers fournis dans src/main/resources/data. */
    public CsvHotelDataSource() {
        this(null);
    }

    /** Charge les fichiers du dossier indiqué. */
    public CsvHotelDataSource(Path directory) {
        this.reader = new CsvFileReader(directory);
    }

    @Override
    public List<Room> loadRooms() throws DataLoadingException {
        List<Room> rooms = new ArrayList<>();
        // number,type,capacity,basePrice,option
        for (CsvLine line : reader.read(ROOMS_FILE, 5)) {
            try {
                rooms.add(RoomFactory.create(
                        RoomType.valueOf(line.field(1).toUpperCase()),
                        line.field(0),
                        Integer.parseInt(line.field(2)),
                        Double.parseDouble(line.field(3)),
                        line.field(4)));
            } catch (IllegalArgumentException e) {
                // Les erreurs de nombre et les valeurs inconnues d'une enum passent aussi ici.
                throw invalidLine(line, e);
            }
        }
        return rooms;
    }

    @Override
    public List<Guest> loadGuests() throws DataLoadingException {
        List<Guest> guests = new ArrayList<>();
        // id,name,email,loyaltyTier
        for (CsvLine line : reader.read(GUESTS_FILE, 4)) {
            try {
                guests.add(new Guest(line.field(0), line.field(1), line.field(2),
                        LoyaltyTier.valueOf(line.field(3).toUpperCase())));
            } catch (IllegalArgumentException e) {
                throw invalidLine(line, e);
            }
        }
        return guests;
    }

    @Override
    public List<ReservationRecord> loadReservations() throws DataLoadingException {
        List<ReservationRecord> records = new ArrayList<>();
        // guestId,roomNumber,checkIn,checkOut,guestCount
        for (CsvLine line : reader.read(RESERVATIONS_FILE, 5)) {
            try {
                DateRange stay = new DateRange(LocalDate.parse(line.field(2)), LocalDate.parse(line.field(3)));
                records.add(new ReservationRecord(line.lineNumber(), line.field(0), line.field(1),
                        stay, Integer.parseInt(line.field(4))));
            } catch (DateTimeParseException | IllegalArgumentException e) {
                throw invalidLine(line, e);
            }
        }
        return records;
    }

    private static DataLoadingException invalidLine(CsvLine line, Exception cause) {
        return new DataLoadingException("Invalid data in " + line.location() + ": " + cause.getMessage(), cause);
    }
}
