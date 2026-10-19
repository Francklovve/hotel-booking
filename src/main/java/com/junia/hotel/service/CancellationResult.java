package com.junia.hotel.service;

import com.junia.hotel.model.Reservation;

public record CancellationResult(Reservation reservation, double fee, double refund) {
}
