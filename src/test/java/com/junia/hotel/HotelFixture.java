package com.junia.hotel;

import com.junia.hotel.model.Guest;
import com.junia.hotel.model.LoyaltyTier;
import com.junia.hotel.model.Reservation;
import com.junia.hotel.model.room.FamilyRoom;
import com.junia.hotel.model.room.Room;
import com.junia.hotel.model.room.StandardRoom;
import com.junia.hotel.model.room.Suite;
import com.junia.hotel.pricing.CancellationPolicyResolver;
import com.junia.hotel.pricing.LongStayDiscount;
import com.junia.hotel.pricing.LoyaltyDiscount;
import com.junia.hotel.pricing.PricingService;
import com.junia.hotel.repository.InMemoryRepository;
import com.junia.hotel.repository.Repository;
import com.junia.hotel.service.AvailabilityService;
import com.junia.hotel.service.ReportService;
import com.junia.hotel.service.ReservationService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

public class HotelFixture {

    public static final LocalDate TODAY = LocalDate.of(2026, 12, 1);

    public final Repository<Room, String> rooms = new InMemoryRepository<>(Room::getNumber);
    public final Repository<Guest, String> guests = new InMemoryRepository<>(Guest::getId);
    public final Repository<Reservation, String> reservations = new InMemoryRepository<>(Reservation::getId);
    public final AvailabilityService availabilityService = new AvailabilityService(rooms, reservations);
    public final ReservationService reservationService;
    public final ReportService reportService = new ReportService(rooms, reservations);

    public HotelFixture() {
        rooms.save(new StandardRoom("102", 2, 80));
        rooms.save(new Suite("201", 2, 200, false));
        rooms.save(new FamilyRoom("301", 4, 120, 25));
        guests.save(new Guest("G001", "Alice", "alice@mail.com", LoyaltyTier.GOLD));
        guests.save(new Guest("G003", "Chloe", "chloe@mail.com", LoyaltyTier.STANDARD));

        PricingService pricing = new PricingService(List.of(new LoyaltyDiscount(), new LongStayDiscount(7, 0.15)));
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        reservationService = new ReservationService(rooms, guests, reservations,
                availabilityService, pricing, new CancellationPolicyResolver(), fixedClock);
    }
}
