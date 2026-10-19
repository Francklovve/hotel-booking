package com.junia.hotel.pricing;

import com.junia.hotel.model.DateRange;
import com.junia.hotel.model.Guest;

public class LongStayDiscount implements DiscountPolicy {

    private final int minimumNights;
    private final double rate;

    public LongStayDiscount(int minimumNights, double rate) {
        if (minimumNights <= 0 || rate < 0 || rate > 1) {
            throw new IllegalArgumentException("Invalid long stay discount configuration");
        }
        this.minimumNights = minimumNights;
        this.rate = rate;
    }

    @Override
    public String name() {
        return "Long stay discount (" + minimumNights + "+ nights)";
    }

    @Override
    public double discountRate(Guest guest, DateRange stay) {
        return stay.nights() >= minimumNights ? rate : 0.0;
    }
}
