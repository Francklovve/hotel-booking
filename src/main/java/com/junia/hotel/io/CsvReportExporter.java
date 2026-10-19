package com.junia.hotel.io;

import com.junia.hotel.exception.DataExportException;
import com.junia.hotel.model.room.RoomType;
import com.junia.hotel.service.Report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Keeps CSV formatting and filesystem errors outside the reporting service. */
public class CsvReportExporter implements ReportExporter {

    @Override
    public void export(Report report, Path target) throws DataExportException {
        if (report == null || target == null) {
            throw new IllegalArgumentException("Report and export target are required");
        }
        List<String> lines = new ArrayList<>();
        lines.add("metric,value");
        lines.add("period_start," + report.period().checkIn());
        lines.add("period_end," + report.period().checkOut());
        for (RoomType type : RoomType.values()) {
            lines.add(String.format(Locale.ROOT, "revenue_%s,%.2f",
                    type, report.revenueByRoomType().getOrDefault(type, 0.0)));
        }
        lines.add(String.format(Locale.ROOT, "total_revenue,%.2f", report.totalRevenue()));
        lines.add(String.format(Locale.ROOT, "occupancy_rate,%.4f", report.occupancyRate()));
        // Quoting protects names containing commas, quotes or line breaks.
        lines.add("best_customer,\"" + report.bestCustomerName().replace("\"", "\"\"") + "\"");

        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(target, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataExportException("Cannot write report to " + target, e);
        }
    }
}
