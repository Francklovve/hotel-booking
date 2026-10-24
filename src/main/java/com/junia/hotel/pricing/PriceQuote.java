package com.junia.hotel.pricing;

/** Détail du prix et de la remise, conservé dans la réservation. */
public record PriceQuote(double subtotal, String discountName, double discountRate, double total) {

    public double discountAmount() {
        return subtotal - total;
    }
}
