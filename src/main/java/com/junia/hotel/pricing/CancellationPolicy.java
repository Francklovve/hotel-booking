package com.junia.hotel.pricing;

import com.junia.hotel.model.DateRange;

import java.time.LocalDate;

/** Strategy for the fraction of a reservation's final price kept on cancellation. */
public interface CancellationPolicy {

    String name();

    double feeRate(DateRange stay, LocalDate cancellationDate);
}
