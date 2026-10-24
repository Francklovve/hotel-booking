package com.junia.hotel.model.room;

import java.time.LocalDate;

/** Le prix de base couvre deux personnes. Chaque personne supplémentaire paie un supplément par nuit. */
public class FamilyRoom extends Room {

    static final int GUESTS_INCLUDED = 2;

    private final double extraGuestFee;

    public FamilyRoom(String number, int capacity, double basePricePerNight, double extraGuestFee) {
        super(number, capacity, basePricePerNight);
        if (extraGuestFee < 0) {
            throw new IllegalArgumentException("Extra guest fee cannot be negative (room " + number + ")");
        }
        this.extraGuestFee = extraGuestFee;
    }

    @Override
    public RoomType getType() {
        return RoomType.FAMILY;
    }

    @Override
    protected double priceForNight(LocalDate night, int guestCount) {
        int extraGuests = Math.max(0, guestCount - GUESTS_INCLUDED);
        return getBasePricePerNight() + extraGuests * extraGuestFee;
    }

    public double getExtraGuestFee() {
        return extraGuestFee;
    }
}
