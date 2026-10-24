package com.junia.hotel.ui;

import com.junia.hotel.exception.BookingException;
import com.junia.hotel.exception.DataExportException;
import com.junia.hotel.io.ReportExporter;
import com.junia.hotel.model.DateRange;
import com.junia.hotel.model.Reservation;
import com.junia.hotel.model.room.Room;
import com.junia.hotel.model.room.RoomType;
import com.junia.hotel.pricing.PriceQuote;
import com.junia.hotel.service.AvailabilityService;
import com.junia.hotel.service.CancellationResult;
import com.junia.hotel.service.Report;
import com.junia.hotel.service.ReportService;
import com.junia.hotel.service.ReservationHistory;
import com.junia.hotel.service.ReservationService;

import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Lit les choix, appelle les services et affiche les résultats ou les erreurs. */
public class ConsoleMenu {

    private final ReservationService reservationService;
    private final AvailabilityService availabilityService;
    private final ReportService reportService;
    private final ReservationHistory history;
    private final ReportExporter reportExporter;
    private final Clock clock;
    private final ConsoleInput input;
    private final PrintStream out;

    public ConsoleMenu(ReservationService reservationService,
                       AvailabilityService availabilityService,
                       ReportService reportService,
                       ReservationHistory history,
                       ReportExporter reportExporter,
                       Clock clock,
                       ConsoleInput input,
                       PrintStream out) {
        this.reservationService = reservationService;
        this.availabilityService = availabilityService;
        this.reportService = reportService;
        this.history = history;
        this.reportExporter = reportExporter;
        this.clock = clock;
        this.input = input;
        this.out = out;
    }

    public void run() {
        while (true) {
            printMenu();
            Optional<Integer> choice = input.readInt("Your choice: ");
            if (choice.isEmpty() || choice.get() == 0) {
                out.println("Goodbye!");
                return;
            }
            try {
                handle(choice.get());
            } catch (BookingException e) {
                out.println("  Operation refused: " + e.getMessage());
            } catch (DataExportException e) {
                out.println("  Cannot export report: " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                out.println("  Invalid request: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        out.println();
        out.println("=== Hotel Booking ===");
        out.println("1. Search available rooms");
        out.println("2. Get a price quote");
        out.println("3. Book a room");
        out.println("4. Cancel a reservation");
        out.println("5. List reservations of a guest");
        out.println("6. List all reservations");
        out.println("7. Reports");
        out.println("8. Show history");
        out.println("9. Export report to CSV");
        out.println("0. Quit");
    }

    private void handle(int choice) throws BookingException, DataExportException {
        switch (choice) {
            case 1 -> searchRooms();
            case 2 -> quote();
            case 3 -> book();
            case 4 -> cancel();
            case 5 -> listGuestReservations();
            case 6 -> reservationService.findAll().forEach(r -> out.println("  " + r));
            case 7 -> reports();
            case 8 -> showHistory();
            case 9 -> exportReport();
            default -> out.println("  Unknown option.");
        }
    }

    private void searchRooms() {
        Optional<DateRange> stay = readStay();
        Optional<Integer> guests = input.readInt("Number of guests: ");
        if (stay.isEmpty() || guests.isEmpty()) {
            return;
        }
        List<Room> rooms = availabilityService.findAvailableRooms(stay.get(), guests.get());
        if (rooms.isEmpty()) {
            out.println("  No room available for these dates.");
        }
        rooms.forEach(room -> out.println("  " + room));
    }

    private void quote() throws BookingException {
        Optional<String> guestId = input.readLine("Guest id: ");
        Optional<String> room = input.readLine("Room number: ");
        Optional<DateRange> stay = readStay();
        Optional<Integer> guests = input.readInt("Number of guests: ");
        if (guestId.isEmpty() || room.isEmpty() || stay.isEmpty() || guests.isEmpty()) {
            return;
        }
        PriceQuote quote = reservationService.quote(guestId.get(), room.get(), stay.get(), guests.get());
        out.printf("  Subtotal: %.2f EUR | %s (-%.0f%%) | Total: %.2f EUR%n",
                quote.subtotal(), quote.discountName(), quote.discountRate() * 100, quote.total());
    }

    private void book() throws BookingException {
        Optional<String> guestId = input.readLine("Guest id: ");
        Optional<String> room = input.readLine("Room number: ");
        Optional<DateRange> stay = readStay();
        Optional<Integer> guests = input.readInt("Number of guests: ");
        if (guestId.isEmpty() || room.isEmpty() || stay.isEmpty() || guests.isEmpty()) {
            return;
        }
        Reservation reservation = reservationService.book(guestId.get(), room.get(), stay.get(), guests.get());
        out.println("  Booked: " + reservation);
    }

    private void cancel() throws BookingException {
        Optional<String> id = input.readLine("Reservation id: ");
        if (id.isPresent()) {
            CancellationResult result = reservationService.cancel(id.get());
            out.println("  Cancelled: " + result.reservation());
            out.printf("  Cancellation fee: %.2f EUR | Refund: %.2f EUR%n", result.fee(), result.refund());
        }
    }

    private void listGuestReservations() throws BookingException {
        Optional<String> guestId = input.readLine("Guest id: ");
        if (guestId.isPresent()) {
            reservationService.findByGuest(guestId.get()).forEach(r -> out.println("  " + r));
        }
    }

    private void reports() {
        Optional<DateRange> period = readStay();
        if (period.isEmpty()) {
            return;
        }
        Report report = reportService.buildReport(period.get());
        out.printf("  Total revenue (all active reservations): %.2f EUR%n", report.totalRevenue());
        for (RoomType type : RoomType.values()) {
            out.printf("    %-8s %.2f EUR%n", type, report.revenueByRoomType().getOrDefault(type, 0.0));
        }
        out.println("  Best customer (all active reservations): " + report.bestCustomerName());
        out.printf("  Occupancy %s: %.1f%%%n", report.period(), report.occupancyRate() * 100);
    }

    private void exportReport() throws DataExportException {
        Optional<DateRange> period = readStay();
        if (period.isEmpty()) {
            return;
        }
        Report report = reportService.buildReport(period.get());
        Path target = Path.of("reports", "report-" + LocalDate.now(clock) + ".csv");
        reportExporter.export(report, target);
        out.println("  Report exported to " + target);
    }

    private void showHistory() {
        List<String> events = history.getEvents();
        if (events.isEmpty()) {
            out.println("  No reservation events yet.");
        }
        events.forEach(event -> out.println("  " + event));
    }

    private Optional<DateRange> readStay() {
        Optional<LocalDate> checkIn = input.readDate("Check-in (YYYY-MM-DD): ");
        Optional<LocalDate> checkOut = input.readDate("Check-out (YYYY-MM-DD): ");
        if (checkIn.isEmpty() || checkOut.isEmpty()) {
            return Optional.empty();
        }
        // Une période invalide est traitée dans run().
        return Optional.of(new DateRange(checkIn.get(), checkOut.get()));
    }
}
