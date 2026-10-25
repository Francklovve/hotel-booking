package com.junia.hotel.pricing;

import com.junia.hotel.model.DateRange;
import com.junia.hotel.model.Guest;

public interface DiscountPolicy {

    String name();

    double discountRate(Guest guest, DateRange stay);
}
