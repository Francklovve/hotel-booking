package com.junia.hotel.pricing;

import com.junia.hotel.model.DateRange;
import com.junia.hotel.model.Guest;

public class LoyaltyDiscount implements DiscountPolicy {

    @Override
    public String name() {
        return "Loyalty discount";
    }

    @Override
    public double discountRate(Guest guest, DateRange stay) {
        return guest.getLoyaltyTier().getDiscountRate();
    }
}
