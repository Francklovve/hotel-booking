package com.junia.hotel.pricing;

import com.junia.hotel.model.DateRange;
import com.junia.hotel.model.Guest;

/**
 * Strategy: one interchangeable discount rule.
 * Adding a new promotion means adding a class, not editing the pricing service (Open/Closed).
 */
public interface DiscountPolicy {

    String name();

    /** Rate between 0 (no discount) and 1, for this guest and this stay. */
    double discountRate(Guest guest, DateRange stay);
}
