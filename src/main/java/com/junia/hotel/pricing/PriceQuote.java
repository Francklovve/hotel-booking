package com.junia.hotel.pricing;

/** Result of a price calculation, kept on the reservation so the price is explainable. */
public record PriceQuote(double subtotal, String discountName, double discountRate, double total) {

    public double discountAmount() {
        return subtotal - total;
    }
}
