package com.junia.hotel.pricing;

public record PriceQuote(double subtotal, String discountName, double discountRate, double total) {

    public double discountAmount() {
        return subtotal - total;
    }
}
