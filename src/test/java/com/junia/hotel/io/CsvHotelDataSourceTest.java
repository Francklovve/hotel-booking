package com.junia.hotel.io;

import com.junia.hotel.exception.DataLoadingException;
import com.junia.hotel.model.Guest;
import com.junia.hotel.model.LoyaltyTier;
import com.junia.hotel.model.room.Room;
import com.junia.hotel.model.room.RoomType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvHotelDataSourceTest {

    @TempDir
    Path directory;

    @Test
    void loadsRoomsSkippingHeaderCommentsAndBlankLines() throws IOException, DataLoadingException {
        Files.writeString(directory.resolve("rooms.csv"), """
                # comment
                number,type,capacity,basePrice,option
                101,STANDARD,1,65,

                201,suite,2,200,true
                """);

        List<Room> rooms = new CsvHotelDataSource(directory).loadRooms();

        assertEquals(2, rooms.size());
        assertEquals(RoomType.SUITE, rooms.get(1).getType());
    }

    @Test
    void loadsGuestsWithTheirLoyaltyTier() throws IOException, DataLoadingException {
        Files.writeString(directory.resolve("guests.csv"), """
                id,name,email,loyaltyTier
                G001,Alice Martin,alice@mail.com,GOLD
                """);

        Guest guest = new CsvHotelDataSource(directory).loadGuests().get(0);

        assertEquals(LoyaltyTier.GOLD, guest.getLoyaltyTier());
    }

    @Test
    void invalidNumberReportsTheFileAndLine() throws IOException {
        Files.writeString(directory.resolve("rooms.csv"), """
                number,type,capacity,basePrice,option
                101,STANDARD,one,65,
                """);

        DataLoadingException exception = assertThrows(DataLoadingException.class,
                () -> new CsvHotelDataSource(directory).loadRooms());
        assertTrue(exception.getMessage().contains("rooms.csv line 2"));
        assertInstanceOf(NumberFormatException.class, exception.getCause());
    }

    @Test
    void unknownRoomTypeIsRefused() throws IOException {
        Files.writeString(directory.resolve("rooms.csv"), """
                number,type,capacity,basePrice,option
                101,PENTHOUSE,2,500,
                """);

        assertThrows(DataLoadingException.class, () -> new CsvHotelDataSource(directory).loadRooms());
    }

    @Test
    void missingColumnsAreRefused() throws IOException {
        Files.writeString(directory.resolve("guests.csv"), """
                id,name,email,loyaltyTier
                G001,Alice
                """);

        assertThrows(DataLoadingException.class, () -> new CsvHotelDataSource(directory).loadGuests());
    }

    @Test
    void invalidDateInReservationIsRefused() throws IOException {
        Files.writeString(directory.resolve("reservations.csv"), """
                guestId,roomNumber,checkIn,checkOut,guestCount
                G001,201,2026-13-40,2026-12-27,2
                """);

        assertThrows(DataLoadingException.class, () -> new CsvHotelDataSource(directory).loadReservations());
    }

    @Test
    void missingFileIsReportedClearly() {
        DataLoadingException exception = assertThrows(DataLoadingException.class,
                () -> new CsvHotelDataSource(directory).loadRooms());
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void bundledDataFilesAreValid() throws DataLoadingException {
        CsvHotelDataSource bundled = new CsvHotelDataSource();

        assertFalse(bundled.loadRooms().isEmpty());
        assertFalse(bundled.loadGuests().isEmpty());
        assertFalse(bundled.loadReservations().isEmpty());
    }
}
