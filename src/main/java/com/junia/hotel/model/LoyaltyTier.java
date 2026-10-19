package com.junia.hotel.model;

/** Loyalty level of a guest; each level grants a discount rate on bookings. */
public enum LoyaltyTier {
    STANDARD(0.0),
    SILVER(0.05),
    GOLD(0.10);

    private final double discountRate;

    LoyaltyTier(double discountRate) {
        this.discountRate = discountRate;
    }

    public double getDiscountRate() {
        return discountRate;
    }
}
