package com.junia.hotel.service;

import com.junia.hotel.model.DateRange;
import com.junia.hotel.model.room.RoomType;

import java.util.Map;

/** Snapshot of global active revenue and the occupancy of the requested period. */
public record Report(DateRange period, Map<RoomType, Double> revenueByRoomType,
                     double totalRevenue, double occupancyRate, String bestCustomerName) {

    public Report {
        if (period == null || revenueByRoomType == null || bestCustomerName == null) {
            throw new IllegalArgumentException("Report period, revenue and best customer name are required");
        }
        // A record is only immutable if its collection components cannot be changed from outside.
        revenueByRoomType = Map.copyOf(revenueByRoomType);
    }
}
