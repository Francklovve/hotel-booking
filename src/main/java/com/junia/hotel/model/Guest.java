package com.junia.hotel.model;

import java.util.Objects;

public class Guest {

    private final String id;
    private final String name;
    private final String email;
    private final LoyaltyTier loyaltyTier;

    public Guest(String id, String name, String email, LoyaltyTier loyaltyTier) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Guest id is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Guest name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email for guest " + id + ": " + email);
        }
        this.id = id;
        this.name = name;
        this.email = email;
        this.loyaltyTier = Objects.requireNonNull(loyaltyTier, "Loyalty tier is required");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LoyaltyTier getLoyaltyTier() {
        return loyaltyTier;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Guest other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + id + ", " + loyaltyTier + ")";
    }
}
