package com.junia.hotel.pricing;

import com.junia.hotel.model.DateRange;

import java.time.LocalDate;

public interface CancellationPolicy {

    String name();

    double feeRate(DateRange stay, LocalDate cancellationDate);
}
