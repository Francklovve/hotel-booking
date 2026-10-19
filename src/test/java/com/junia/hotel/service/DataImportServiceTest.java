package com.junia.hotel.service;

import com.junia.hotel.exception.DataLoadingException;
import com.junia.hotel.io.HotelDataSource;
import com.junia.hotel.io.ReservationRecord;
import com.junia.hotel.model.Guest;
import com.junia.hotel.model.LoyaltyTier;
import com.junia.hotel.model.room.Room;
import com.junia.hotel.model.room.StandardRoom;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.junia.hotel.TestData.stay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Uses a fake data source: thanks to the HotelDataSource interface the import logic
 * can be tested without any file.
 */
class DataImportServiceTest {

    private static class FakeDataSource implements HotelDataSource {
        @Override
        public List<Room> loadRooms() {
            return List.of(new StandardRoom("900", 2, 50));
        }

        @Override
        public List<Guest> loadGuests() {
            return List.of(new Guest("G900", "Zoe", "zoe@mail.com", LoyaltyTier.STANDARD));
        }

        @Override
        public List<ReservationRecord> loadReservations() {
            return List.of(
                    new ReservationRecord(2, "G900", "900", stay("2026-12-10", "2026-12-12"), 2),
                    new ReservationRecord(3, "G900", "900", stay("2026-12-11", "2026-12-13"), 2),
                    new ReservationRecord(4, "G404", "900", stay("2026-12-20", "2026-12-21"), 1));
        }
    }

    @Test
    void validLinesAreImportedAndInvalidOnesReportedWithTheirLineNumber() throws DataLoadingException {
        HotelFixture hotel = new HotelFixture();
        DataImportService importer = new DataImportService(new FakeDataSource(),
                hotel.rooms, hotel.guests, hotel.reservationService);

        ImportResult result = importer.importAll();

        assertEquals(1, result.rooms());
        assertEquals(1, result.reservations());
        assertEquals(2, result.rejectedReservations().size());
        assertTrue(result.rejectedReservations().get(0).contains("line 3"));
        assertTrue(result.rejectedReservations().get(1).contains("G404"));
    }
}
