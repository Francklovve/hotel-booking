package com.junia.hotel.pricing;

import com.junia.hotel.model.DateRange;
import com.junia.hotel.model.Guest;

/** Règle de remise utilisée par le calcul du prix. */
public interface DiscountPolicy {

    String name();

    /** Remise entre 0 et 1 pour ce client et ce séjour. */
    double discountRate(Guest guest, DateRange stay);
}
