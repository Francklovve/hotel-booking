package com.junia.hotel.pricing;

import com.junia.hotel.model.DateRange;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FlexibleCancellationPolicy implements CancellationPolicy {

    @Override
    public String name() {
        return "Flexible";
    }

    @Override
    public double feeRate(DateRange stay, LocalDate cancellationDate) {
        if (stay == null || cancellationDate == null) {
            throw new IllegalArgumentException("Stay and cancellation date are required");
        }
        long daysBeforeCheckIn = ChronoUnit.DAYS.between(cancellationDate, stay.checkIn());
        return daysBeforeCheckIn >= 2 ? 0.0 : 0.5;
    }
}
