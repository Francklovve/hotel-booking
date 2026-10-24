package com.junia.hotel.pricing;

import com.junia.hotel.model.DateRange;

import java.time.LocalDate;

/** Part du prix final à garder en cas d'annulation. */
public interface CancellationPolicy {

    String name();

    double feeRate(DateRange stay, LocalDate cancellationDate);
}
