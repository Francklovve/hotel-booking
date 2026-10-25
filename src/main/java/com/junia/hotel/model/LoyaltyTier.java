package com.junia.hotel.model;

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
