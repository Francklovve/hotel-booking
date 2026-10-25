package com.junia.hotel.model.room;

public final class RoomFactory {

    private RoomFactory() {
    }

    public static Room create(RoomType type, String number, int capacity, double basePrice, String option) {
        return switch (type) {
            case STANDARD -> new StandardRoom(number, capacity, basePrice);
            case SUITE -> new Suite(number, capacity, basePrice, Boolean.parseBoolean(option));
            case FAMILY -> new FamilyRoom(number, capacity, basePrice, parseFee(option, number));
        };
    }

    private static double parseFee(String option, String number) {
        if (option == null || option.isBlank()) {
            throw new IllegalArgumentException("Family room " + number + " needs an extra guest fee");
        }
        return Double.parseDouble(option);
    }
}
