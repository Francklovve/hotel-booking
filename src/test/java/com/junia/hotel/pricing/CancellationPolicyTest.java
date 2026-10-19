package com.junia.hotel.pricing;

import com.junia.hotel.model.DateRange;
import com.junia.hotel.model.room.RoomType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.junia.hotel.TestData.stay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CancellationPolicyTest {

    @Test
    void flexibleCancellationOnArrivalDayCostsHalfThePrice() {
        CancellationPolicy policy = new FlexibleCancellationPolicy();
        DateRange stay = stay("2026-12-10", "2026-12-12");

        double rate = policy.feeRate(stay, LocalDate.of(2026, 12, 10));

        assertEquals(0.5, rate, 0.0001);
    }

    @Test
    void nonRefundableCancellationCostsTheWholePriceWhateverTheDate() {
        CancellationPolicy policy = new NonRefundablePolicy();
        DateRange stay = stay("2026-12-10", "2026-12-12");

        double earlyRate = policy.feeRate(stay, LocalDate.of(2026, 1, 1));
        double arrivalRate = policy.feeRate(stay, LocalDate.of(2026, 12, 10));

        assertEquals(1.0, earlyRate, 0.0001);
        assertEquals(1.0, arrivalRate, 0.0001);
    }

    @Test
    void bothPoliciesRejectMissingStayOrCancellationDate() {
        CancellationPolicy flexible = new FlexibleCancellationPolicy();
        CancellationPolicy nonRefundable = new NonRefundablePolicy();

        assertThrows(IllegalArgumentException.class,
                () -> flexible.feeRate(null, LocalDate.of(2026, 12, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> flexible.feeRate(stay("2026-12-10", "2026-12-12"), null));
        assertThrows(IllegalArgumentException.class,
                () -> nonRefundable.feeRate(null, LocalDate.of(2026, 12, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> nonRefundable.feeRate(stay("2026-12-10", "2026-12-12"), null));
    }

    @Test
    void resolverSelectsFlexiblePolicyForStandardAndFamilyAndNonRefundableForSuite() {
        CancellationPolicy flexible = new FlexibleCancellationPolicy();
        CancellationPolicy nonRefundable = new NonRefundablePolicy();
        CancellationPolicyResolver resolver = new CancellationPolicyResolver(flexible, nonRefundable);

        assertSame(flexible, resolver.resolve(RoomType.STANDARD));
        assertSame(flexible, resolver.resolve(RoomType.FAMILY));
        assertSame(nonRefundable, resolver.resolve(RoomType.SUITE));
    }

    @Test
    void resolverRejectsMissingStrategiesOrRoomType() {
        CancellationPolicyResolver resolver = new CancellationPolicyResolver();

        assertThrows(IllegalArgumentException.class,
                () -> new CancellationPolicyResolver(null, new NonRefundablePolicy()));
        assertThrows(IllegalArgumentException.class,
                () -> new CancellationPolicyResolver(new FlexibleCancellationPolicy(), null));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(null));
    }
}
