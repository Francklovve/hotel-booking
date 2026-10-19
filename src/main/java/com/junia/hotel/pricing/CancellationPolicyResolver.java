package com.junia.hotel.pricing;

import com.junia.hotel.model.room.RoomType;

import java.util.EnumMap;
import java.util.Map;

/** Keeps cancellation rules outside Room, whose responsibility is room pricing and capacity. */
public class CancellationPolicyResolver {

    private final Map<RoomType, CancellationPolicy> policies = new EnumMap<>(RoomType.class);

    public CancellationPolicyResolver() {
        this(new FlexibleCancellationPolicy(), new NonRefundablePolicy());
    }

    public CancellationPolicyResolver(CancellationPolicy flexible, CancellationPolicy nonRefundable) {
        if (flexible == null || nonRefundable == null) {
            throw new IllegalArgumentException("Cancellation policies are required");
        }
        policies.put(RoomType.STANDARD, flexible);
        policies.put(RoomType.FAMILY, flexible);
        policies.put(RoomType.SUITE, nonRefundable);
    }

    public CancellationPolicy resolve(RoomType type) {
        if (type == null) {
            throw new IllegalArgumentException("Room type is required");
        }
        return policies.get(type);
    }
}
