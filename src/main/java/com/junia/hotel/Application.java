package com.junia.hotel;

import com.junia.hotel.exception.DataLoadingException;
import com.junia.hotel.io.CsvHotelDataSource;
import com.junia.hotel.io.CsvReportExporter;
import com.junia.hotel.io.HotelDataSource;
import com.junia.hotel.io.ReportExporter;
import com.junia.hotel.model.Guest;
import com.junia.hotel.model.Reservation;
import com.junia.hotel.model.room.Room;
import com.junia.hotel.pricing.CancellationPolicyResolver;
import com.junia.hotel.pricing.LongStayDiscount;
import com.junia.hotel.pricing.LoyaltyDiscount;
import com.junia.hotel.pricing.PricingService;
import com.junia.hotel.repository.InMemoryRepository;
import com.junia.hotel.repository.Repository;
import com.junia.hotel.service.AvailabilityService;
import com.junia.hotel.service.DataImportService;
import com.junia.hotel.service.ImportResult;
import com.junia.hotel.service.ReportService;
import com.junia.hotel.service.ReservationHistory;
import com.junia.hotel.service.ReservationService;
import com.junia.hotel.ui.ConsoleInput;
import com.junia.hotel.ui.ConsoleMenu;
import com.junia.hotel.ui.ConsoleNotifier;

import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.Scanner;

/**
 * Composition root: the only place that chooses concrete implementations and wires them.
 * Usage: java -jar hotel-booking.jar [dataDirectory]
 */
public class Application {

    public static void main(String[] args) {
        HotelDataSource dataSource = args.length > 0
                ? new CsvHotelDataSource(Path.of(args[0]))
                : new CsvHotelDataSource();

        Repository<Room, String> rooms = new InMemoryRepository<>(Room::getNumber);
        Repository<Guest, String> guests = new InMemoryRepository<>(Guest::getId);
        Repository<Reservation, String> reservations = new InMemoryRepository<>(Reservation::getId);

        PricingService pricingService = new PricingService(List.of(
                new LoyaltyDiscount(),
                new LongStayDiscount(7, 0.15)));
        AvailabilityService availabilityService = new AvailabilityService(rooms, reservations);
        Clock clock = Clock.systemDefaultZone();
        ReservationService reservationService = new ReservationService(rooms, guests, reservations,
                availabilityService, pricingService, new CancellationPolicyResolver(), clock);
        ReportService reportService = new ReportService(rooms, reservations);
        ReservationHistory history = new ReservationHistory();
        reservationService.subscribe(new ConsoleNotifier(System.out));
        reservationService.subscribe(history);
        ReportExporter reportExporter = new CsvReportExporter();

        try {
            ImportResult result = new DataImportService(dataSource, rooms, guests, reservationService).importAll();
            System.out.printf("Loaded %d rooms, %d guests, %d reservations.%n",
                    result.rooms(), result.guests(), result.reservations());
            result.rejectedReservations().forEach(message -> System.out.println("  Rejected: " + message));
        } catch (DataLoadingException e) {
            System.err.println("Cannot start the application: " + e.getMessage());
            System.exit(1);
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleInput input = new ConsoleInput(scanner, System.out);
            new ConsoleMenu(reservationService, availabilityService, reportService, history,
                    reportExporter, clock, input, System.out).run();
        }
    }
}
