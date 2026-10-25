package com.junia.hotel.io;

import com.junia.hotel.exception.DataExportException;
import com.junia.hotel.model.room.RoomType;
import com.junia.hotel.service.Report;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.junia.hotel.TestData.stay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvReportExporterTest {

    @TempDir
    Path directory;

    private final ReportExporter exporter = new CsvReportExporter();

    @Test
    void exportWritesOrderedUtf8CsvWithEscapedNamesAndLocaleIndependentNumbers()
            throws IOException, DataExportException {
        Report report = new Report(stay("2026-12-10", "2026-12-12"),
                Map.of(RoomType.SUITE, 200.0, RoomType.STANDARD, 123.45),
                323.45, 0.125, "Élodie, \"VIP\"");
        Path target = directory.resolve("report.csv");
        Locale previousLocale = Locale.getDefault();

        try {
            Locale.setDefault(Locale.FRANCE);
            exporter.export(report, target);
        } finally {
            Locale.setDefault(previousLocale);
        }

        assertEquals(List.of(
                "metric,value",
                "period_start,2026-12-10",
                "period_end,2026-12-12",
                "revenue_STANDARD,123.45",
                "revenue_SUITE,200.00",
                "revenue_FAMILY,0.00",
                "total_revenue,323.45",
                "occupancy_rate,0.1250",
                "best_customer,\"Élodie, \"\"VIP\"\"\""),
                Files.readAllLines(target, StandardCharsets.UTF_8));
    }

    @Test
    void exportCreatesMissingParentDirectories() throws IOException, DataExportException {
        Path target = directory.resolve("reports").resolve("2026").resolve("report.csv");

        exporter.export(emptyReport(), target);

        assertTrue(Files.isRegularFile(target));
        assertEquals("metric,value", Files.readAllLines(target, StandardCharsets.UTF_8).get(0));
    }

    @Test
    void emptyReportStillExportsAllRoomTypesAndNoBestCustomer() throws IOException, DataExportException {
        Path target = directory.resolve("empty.csv");

        exporter.export(emptyReport(), target);

        assertEquals(List.of(
                "metric,value",
                "period_start,2026-12-10",
                "period_end,2026-12-12",
                "revenue_STANDARD,0.00",
                "revenue_SUITE,0.00",
                "revenue_FAMILY,0.00",
                "total_revenue,0.00",
                "occupancy_rate,0.0000",
                "best_customer,\"None\""),
                Files.readAllLines(target, StandardCharsets.UTF_8));
    }

    @Test
    void fileUsedAsParentDirectoryProducesExportExceptionWithOriginalCause() throws IOException {
        Path blocker = directory.resolve("blocker");
        Files.writeString(blocker, "Existing file", StandardCharsets.UTF_8);
        Path target = blocker.resolve("report.csv");

        DataExportException exception = assertThrows(DataExportException.class,
                () -> exporter.export(emptyReport(), target));

        assertInstanceOf(IOException.class, exception.getCause());
        assertEquals("Existing file", Files.readString(blocker, StandardCharsets.UTF_8));
    }

    @Test
    void directoryUsedAsTargetProducesExportExceptionWithOriginalCause() {
        DataExportException exception = assertThrows(DataExportException.class,
                () -> exporter.export(emptyReport(), directory));

        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void exportRejectsMissingReportOrTarget() {
        assertThrows(IllegalArgumentException.class,
                () -> exporter.export(null, directory.resolve("report.csv")));
        assertThrows(IllegalArgumentException.class, () -> exporter.export(emptyReport(), null));
    }

    private Report emptyReport() {
        return new Report(stay("2026-12-10", "2026-12-12"), Map.of(), 0.0, 0.0, "None");
    }
}
