package com.junia.hotel.pricing;

import com.junia.hotel.model.DateRange;

import java.time.LocalDate;

public class NonRefundablePolicy implements CancellationPolicy {

    @Override
    public String name() {
        return "Non-refundable";
    }

    @Override
    public double feeRate(DateRange stay, LocalDate cancellationDate) {
        if (stay == null || cancellationDate == null) {
            throw new IllegalArgumentException("Stay and cancellation date are required");
        }
        return 1.0;
    }
}
