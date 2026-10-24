package com.junia.hotel.model.room;

import java.time.DayOfWeek;
import java.time.LocalDate;

/** Majoration le vendredi et le samedi, avec un petit-déjeuner facturé par personne si l'option est activée. */
public class Suite extends Room {

    static final double WEEKEND_SURCHARGE_RATE = 0.25;
    static final double BREAKFAST_PRICE_PER_GUEST = 15.0;

    private final boolean breakfastIncluded;

    public Suite(String number, int capacity, double basePricePerNight, boolean breakfastIncluded) {
        super(number, capacity, basePricePerNight);
        this.breakfastIncluded = breakfastIncluded;
    }

    @Override
    public RoomType getType() {
        return RoomType.SUITE;
    }

    @Override
    protected double priceForNight(LocalDate night, int guestCount) {
        double price = getBasePricePerNight();
        if (isWeekendNight(night)) {
            price += getBasePricePerNight() * WEEKEND_SURCHARGE_RATE;
        }
        if (breakfastIncluded) {
            price += BREAKFAST_PRICE_PER_GUEST * guestCount;
        }
        return price;
    }

    private boolean isWeekendNight(LocalDate night) {
        DayOfWeek day = night.getDayOfWeek();
        return day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY;
    }

    public boolean isBreakfastIncluded() {
        return breakfastIncluded;
    }
}
