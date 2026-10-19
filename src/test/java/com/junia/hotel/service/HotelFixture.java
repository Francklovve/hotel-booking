package com.junia.hotel.service;

import com.junia.hotel.model.Guest;
import com.junia.hotel.model.LoyaltyTier;
import com.junia.hotel.model.Reservation;
import com.junia.hotel.model.room.FamilyRoom;
import com.junia.hotel.model.room.Room;
import com.junia.hotel.model.room.StandardRoom;
import com.junia.hotel.model.room.Suite;
import com.junia.hotel.pricing.LongStayDiscount;
import com.junia.hotel.pricing.LoyaltyDiscount;
import com.junia.hotel.pricing.PricingService;
import com.junia.hotel.repository.InMemoryRepository;
import com.junia.hotel.repository.Repository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/** A small hotel wired with real services and a fixed "today" (2026-12-01). */
class HotelFixture {

    static final LocalDate TODAY = LocalDate.of(2026, 12, 1);

    final Repository<Room, String> rooms = new InMemoryRepository<>(Room::getNumber);
    final Repository<Guest, String> guests = new InMemoryRepository<>(Guest::getId);
    final Repository<Reservation, String> reservations = new InMemoryRepository<>(Reservation::getId);
    final AvailabilityService availabilityService = new AvailabilityService(rooms, reservations);
    final ReservationService reservationService;
    final ReportService reportService = new ReportService(rooms, reservations);

    HotelFixture() {
        rooms.save(new StandardRoom("102", 2, 80));
        rooms.save(new Suite("201", 2, 200, false));
        rooms.save(new FamilyRoom("301", 4, 120, 25));
        guests.save(new Guest("G001", "Alice", "alice@mail.com", LoyaltyTier.GOLD));
        guests.save(new Guest("G003", "Chloe", "chloe@mail.com", LoyaltyTier.STANDARD));

        PricingService pricing = new PricingService(List.of(new LoyaltyDiscount(), new LongStayDiscount(7, 0.15)));
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        reservationService = new ReservationService(rooms, guests, reservations,
                availabilityService, pricing, fixedClock);
    }
}
