package com.junia.hotel.pricing;

import com.junia.hotel.model.Guest;
import com.junia.hotel.model.LoyaltyTier;
import com.junia.hotel.model.room.Room;
import com.junia.hotel.model.room.StandardRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.junia.hotel.TestData.stay;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {

    private PricingService pricingService;
    private Room room;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService(List.of(new LoyaltyDiscount(), new LongStayDiscount(7, 0.15)));
        room = new StandardRoom("102", 2, 100);
    }

    @Test
    void standardGuestShortStayPaysFullPrice() {
        Guest guest = new Guest("G1", "Chloe", "chloe@mail.com", LoyaltyTier.STANDARD);

        PriceQuote quote = pricingService.quote(room, guest, stay("2026-12-14", "2026-12-17"), 1);

        assertEquals(300, quote.total(), 0.001);
        assertEquals("None", quote.discountName());
    }

    @Test
    void goldGuestGetsLoyaltyDiscount() {
        Guest guest = new Guest("G1", "Alice", "alice@mail.com", LoyaltyTier.GOLD);

        PriceQuote quote = pricingService.quote(room, guest, stay("2026-12-14", "2026-12-17"), 1);

        assertEquals(270, quote.total(), 0.001);
        assertEquals(30, quote.discountAmount(), 0.001);
    }

    @Test
    void longStayDiscountStartsAtExactlySevenNights() {
        Guest guest = new Guest("G1", "Chloe", "chloe@mail.com", LoyaltyTier.STANDARD);

        PriceQuote quote = pricingService.quote(room, guest, stay("2026-12-01", "2026-12-08"), 1);

        assertEquals(595, quote.total(), 0.001);
    }

    @Test
    void sixNightsDoNotGetLongStayDiscount() {
        Guest guest = new Guest("G1", "Chloe", "chloe@mail.com", LoyaltyTier.STANDARD);

        PriceQuote quote = pricingService.quote(room, guest, stay("2026-12-01", "2026-12-07"), 1);

        assertEquals(600, quote.total(), 0.001);
    }

    @Test
    void discountsDoNotStackOnlyTheBestOneApplies() {
        Guest guest = new Guest("G1", "Alice", "alice@mail.com", LoyaltyTier.GOLD);

        PriceQuote quote = pricingService.quote(room, guest, stay("2026-12-01", "2026-12-08"), 1);

        assertEquals(0.15, quote.discountRate(), 0.0001);
        assertEquals(595, quote.total(), 0.001);
    }

    @Test
    void newDiscountCanBeAddedWithoutChangingThePricingService() {
        DiscountPolicy christmasPromo = new DiscountPolicy() {
            @Override
            public String name() {
                return "Christmas";
            }

            @Override
            public double discountRate(Guest guest, com.junia.hotel.model.DateRange stay) {
                return 0.5;
            }
        };
        PricingService service = new PricingService(List.of(new LoyaltyDiscount(), christmasPromo));
        Guest guest = new Guest("G1", "Alice", "alice@mail.com", LoyaltyTier.GOLD);

        PriceQuote quote = service.quote(room, guest, stay("2026-12-24", "2026-12-25"), 1);

        assertEquals("Christmas", quote.discountName());
        assertEquals(50, quote.total(), 0.001);
    }
}
