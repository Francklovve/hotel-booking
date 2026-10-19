# Architecture

```mermaid
classDiagram
    class Room {
        <<abstract>>
        -String number
        -int capacity
        -double basePricePerNight
        +getType() RoomType*
        #priceForNight(LocalDate, int) double*
        +priceForStay(DateRange, int) double
        +canAccommodate(int) boolean
    }
    Room <|-- StandardRoom
    Room <|-- Suite
    Room <|-- FamilyRoom
    class Suite { -boolean breakfastIncluded }
    class FamilyRoom { -double extraGuestFee }

    class DiscountPolicy {
        <<interface>>
        +name() String
        +discountRate(Guest, DateRange) double
    }
    DiscountPolicy <|.. LoyaltyDiscount
    DiscountPolicy <|.. LongStayDiscount
    PricingService o-- DiscountPolicy

    class Repository~T,ID~ { <<interface>> }
    Repository <|.. InMemoryRepository

    class HotelDataSource { <<interface>> }
    HotelDataSource <|.. CsvHotelDataSource

    Reservation --> Guest
    Reservation --> Room
    Reservation --> DateRange
    Reservation --> PriceQuote

    ReservationService --> Repository
    ReservationService --> AvailabilityService
    ReservationService --> PricingService
    ReportService --> Repository
    DataImportService --> HotelDataSource
    DataImportService --> ReservationService
    ConsoleMenu --> ReservationService
    ConsoleMenu --> ReportService
    BookingException <|-- RoomUnavailableException
    BookingException <|-- CapacityExceededException
    BookingException <|-- RoomNotFoundException
```

## Application flow

1. `Application` creates repositories, pricing rules and services (composition root).
2. `DataImportService` loads rooms, guests, then existing reservations from the CSV files.
3. Each existing reservation is checked by `ReservationService` (conflicts, capacity); invalid ones are reported.
4. `ConsoleMenu` lets the user search, quote, book, cancel and see reports.
5. Business errors (`BookingException`) are caught in the menu and displayed; technical errors at startup stop the app with a clear message.
