package com.junia.hotel.pricing;

import com.junia.hotel.model.DateRange;
import com.junia.hotel.model.Guest;
import com.junia.hotel.model.room.Room;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Calcule le prix du séjour en gardant la meilleure remise, sans les cumuler. */
public class PricingService {

    private final List<DiscountPolicy> discountPolicies;

    public PricingService(List<DiscountPolicy> discountPolicies) {
        this.discountPolicies = List.copyOf(discountPolicies);
    }

    public PriceQuote quote(Room room, Guest guest, DateRange stay, int guestCount) {
        double subtotal = room.priceForStay(stay, guestCount);

        Optional<DiscountPolicy> best = discountPolicies.stream()
                .filter(policy -> policy.discountRate(guest, stay) > 0)
                .max(Comparator.comparingDouble(policy -> policy.discountRate(guest, stay)));

        if (best.isEmpty()) {
            return new PriceQuote(round(subtotal), "None", 0.0, round(subtotal));
        }
        double rate = best.get().discountRate(guest, stay);
        return new PriceQuote(round(subtotal), best.get().name(), rate, round(subtotal * (1 - rate)));
    }

    private static double round(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}
