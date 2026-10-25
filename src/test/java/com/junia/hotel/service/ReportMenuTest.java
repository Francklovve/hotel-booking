package com.junia.hotel.service;

import com.junia.hotel.exception.BookingException;
import com.junia.hotel.exception.DataExportException;
import com.junia.hotel.io.ReportExporter;
import com.junia.hotel.model.room.RoomType;
import com.junia.hotel.ui.ConsoleInput;
import com.junia.hotel.ui.ConsoleMenu;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Scanner;

import static com.junia.hotel.TestData.stay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportMenuTest {

    @Test
    void exportOptionBuildsTheRequestedReportAndUsesTheInjectedDate() throws BookingException {
        HotelFixture hotel = new HotelFixture();
        hotel.reservationService.book("G003", "102", stay("2026-12-10", "2026-12-12"), 2);
        CapturingExporter exporter = new CapturingExporter();

        String output = runMenu(hotel, exporter, "9\n2026-12-10\n2026-12-12\n0\n");

        assertEquals(1, exporter.calls);
        assertEquals(Path.of("reports", "report-2026-12-01.csv"), exporter.target);
        assertEquals(stay("2026-12-10", "2026-12-12"), exporter.report.period());
        assertEquals(160.0, exporter.report.totalRevenue());
        assertEquals(160.0, exporter.report.revenueByRoomType().get(RoomType.STANDARD));
        assertEquals(0.3333333333, exporter.report.occupancyRate(), 0.0000000001);
        assertEquals("Chloe", exporter.report.bestCustomerName());
        assertTrue(output.contains("Report exported to " + Path.of("reports", "report-2026-12-01.csv")));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void failedExportDisplaysTheErrorAndAllowsAnotherMenuChoice() {
        HotelFixture hotel = new HotelFixture();
        CapturingExporter exporter = new CapturingExporter();
        exporter.failure = new IOException("disk unavailable");

        String output = runMenu(hotel, exporter, "9\n2026-12-10\n2026-12-12\n8\n0\n");

        assertEquals(1, exporter.calls);
        assertTrue(output.contains("Cannot export report: disk unavailable"));
        assertTrue(output.contains("No reservation events yet."));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void endOfInputBeforeThePeriodIsCompleteDoesNotExport() {
        HotelFixture hotel = new HotelFixture();
        CapturingExporter exporter = new CapturingExporter();

        String output = runMenu(hotel, exporter, "9\n2026-12-10\n");

        assertEquals(0, exporter.calls);
        assertNull(exporter.report);
        assertNull(exporter.target);
        assertTrue(output.contains("Goodbye!"));
    }

    private String runMenu(HotelFixture hotel, ReportExporter exporter, String commands) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Clock clock = Clock.fixed(Instant.parse("2026-12-01T00:00:00Z"), ZoneOffset.UTC);
        try (Scanner scanner = new Scanner(commands);
             PrintStream out = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            ConsoleInput input = new ConsoleInput(scanner, out);
            ConsoleMenu menu = new ConsoleMenu(hotel.reservationService, hotel.availabilityService,
                    hotel.reportService, new ReservationHistory(), exporter, clock, input, out);
            menu.run();
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    private static class CapturingExporter implements ReportExporter {
        private int calls;
        private Report report;
        private Path target;
        private IOException failure;

        @Override
        public void export(Report report, Path target) throws DataExportException {
            calls++;
            this.report = report;
            this.target = target;
            if (failure != null) {
                throw new DataExportException(failure.getMessage(), failure);
            }
        }
    }
}
