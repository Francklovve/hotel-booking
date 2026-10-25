# Hotel Booking System

Final project for Object-Oriented Programming in Java, JUNIA M1.

## 1. Purpose

This is a console program for managing hotel bookings. It lets you find a free room, get a price,
book or cancel a stay, and check the hotel's reports. Data is loaded from CSV files at startup.
Bookings made during a session are kept in memory, so they are lost when the program closes.

## 2. How to run

You need JDK 17 or newer. The project compiles for Java 17.
The Maven Wrapper uses Maven 3.9.11, so there is no need to install Maven separately.
The first run needs an internet connection to download Maven and the dependencies.
Set `JAVA_HOME` to your JDK folder.

From PowerShell, in the project folder:

```powershell
.\mvnw.cmd package
java -jar target/hotel-booking-1.0.0.jar
```

The program uses the files in `src/main/resources/data`. To use another folder:

```powershell
java -jar target/hotel-booking-1.0.0.jar path/to/data/folder
```

That folder must contain `rooms.csv`, `guests.csv` and `reservations.csv`.
In IntelliJ, open the project and run `com.junia.hotel.Application`.
On Linux or macOS, use `./mvnw` instead of `.\mvnw.cmd`. Choose option 0 to quit.

## 3. Main features

| Menu | What it does |
|---|---|
| 1 | Find rooms that are free for the stay and large enough, sorted by price |
| 2 | Calculate a price with the best available discount |
| 3 | Book a room for an existing guest |
| 4 | Cancel a reservation and show the fee and refund |
| 5 / 6 | List one guest's reservations or all reservations |
| 7 | Show revenue by room type, total revenue, occupancy and the best customer |
| 8 | Show the session history |
| 9 | Export a report to CSV |

A new booking cannot start in the past. The guest and room must exist, the room must have enough
space, and the dates must not overlap another active booking. At startup, imported reservations
can be in the past, but the other checks still apply. One booking in the demo file conflicts with
another one: it is reported and skipped so the program can still start.

STANDARD and FAMILY rooms have free cancellation at least two calendar days before check-in.
Later cancellations cost 50%. SUITE rooms always cost 100% to cancel. Each reservation can be
cancelled only once. Fees use the price saved at booking, after the discount, and are rounded to cents.

A successful booking or cancellation prints a simulated confirmation email and adds a history
entry. Imports and failed operations do not add entries. The history lasts for the session.

Revenue and the best customer are calculated from all active reservations. Only occupancy uses
the period entered in the menu. Cancelled reservations and their fees are left out of the report.
The export goes to `reports/report-<date>.csv`, in UTF-8. Exporting again on the same day replaces
the file. It has `metric,value` columns with the period, each room type's revenue, the total,
occupancy as a value from 0 to 1, and the best customer's name (`None` if there is no customer).

## 4. Main classes

`Application` creates the objects and connects them. `ConsoleMenu` handles the menu, with
`ConsoleInput` for reading and checking input.

`DateRange`, `Guest` and `Reservation` hold the booking data. The `model.room` package contains
`Room`, `StandardRoom`, `Suite`, `FamilyRoom`, `RoomType` and `RoomFactory`.

`ReservationService` handles bookings and cancellations. `AvailabilityService` checks free rooms,
`PricingService` calculates prices, and `ReportService` calculates the report indicators. `ReservationHistory`
keeps the session's booking and cancellation events.

Services use `Repository` to access stored objects; `InMemoryRepository` stores them in a map.
`DataImportService` loads the starting data through `HotelDataSource` and `CsvHotelDataSource`.
`CsvReportExporter` writes the reports through the `ReportExporter` interface.

The class diagram is in [docs/architecture.md](docs/architecture.md).

## 5. Technical decisions

1. **Keep cancellation separate from room pricing.** `CancellationPolicyResolver` chooses the
   policy for each `RoomType`, so `Room` only handles the room's price and capacity.
2. **Use different exceptions for refusals and invalid input.** Business refusals extend
   `BookingException`. Invalid dates and arguments use `IllegalArgumentException` or a subclass.
   Cancelling twice uses `IllegalStateException`.
3. **Exclude the check-out day.** A stay uses `[checkIn, checkOut)`, so one guest can leave on the
   day another arrives without creating a conflict.
4. **Pass a `Clock` to the service and menu.** This lets tests set today's date when checking
   past bookings, cancellation fees and report filenames.
5. **Apply one discount.** Discounts do not add up: the largest one is used. `PriceQuote` keeps
   its name and the final price, which is saved with the reservation.

## 6. Inheritance

`StandardRoom`, `Suite` and `FamilyRoom` extend the abstract class `Room`. They share its number,
capacity and base price, but each defines its own price per night. `Suite` adds `breakfastIncluded`
and `FamilyRoom` adds `extraGuestFee`. `StandardRoom` adds no fields.

The business exceptions also use inheritance: for example, `RoomUnavailableException` and
`CapacityExceededException` extend `BookingException`.

## 7. Polymorphism

`Room.priceForStay()` adds up the nights by calling `priceForNight()`. This is the Template Method
pattern. The call uses the actual room type: a standard room has a fixed price, a suite costs 25%
more on Friday and Saturday and can include breakfast, and a family room charges for extra guests.
`RoomPricingTest.sameStayHasDifferentPricesDependingOnTheActualRoomType` checks this difference.

`PricingService` uses `DiscountPolicy` references to compare discounts. `ReservationService`
uses a `CancellationPolicy` reference to calculate the fee. Both services can call the rules
without checking which implementation they received.

## 8. Abstract class and interfaces

`Room` is abstract because the three room types share fields and the calculation over a stay,
but each must provide its price for one night. `BookingException` is also abstract: the menu
can catch it while the concrete exceptions describe the reason for the refusal.

`DiscountPolicy` and `CancellationPolicy` use the Strategy pattern for the pricing rules.
`ReservationListener` uses Observer: `ConsoleNotifier` and `ReservationHistory` receive successful
booking and cancellation events. Listeners can subscribe and unsubscribe, without duplicates.

`Repository<T, ID>` covers storage, `HotelDataSource` covers loading, and `ReportExporter` covers
exporting. `CancellationResult` and `Report` are records used for results. The report copies its
revenue map, and the history returns a list copy, so callers cannot change those collections.

## 9. Custom exceptions

| Exception | Type | When it is used |
|---|---|---|
| `BookingException` | Checked, abstract | Base type for business refusals |
| `RoomUnavailableException` | Checked | Another reservation already occupies the room |
| `CapacityExceededException` | Checked | Too many guests for the room |
| `RoomNotFoundException`, `GuestNotFoundException`, `ReservationNotFoundException` | Checked | The requested ID does not exist |
| `InvalidDateRangeException` | Unchecked | Check-out is not after check-in, or a new stay starts in the past |
| `DataLoadingException` | Checked | An input file is missing, unreadable or malformed |
| `DataExportException` | Checked | The report cannot be written or its directory cannot be created |

The menu catches business errors and invalid requests and displays their messages. Loading and
export exceptions keep the original cause when one is available. A loading error stops startup;
an export error returns to the menu. Cancelling an already cancelled reservation raises
`IllegalStateException`.

## 10. Data loading

`DataImportService` loads rooms, guests, then reservations through `HotelDataSource`.
`CsvHotelDataSource` reads from the bundled files or the folder passed on the command line.
Files are closed with try-with-resources.

Input files use commas and do not support quoted fields. The header, blank lines and lines
starting with `#` are skipped. In `rooms.csv`, the `option` column means breakfast included
(`true` or `false`) for a suite, or the extra guest fee per night for a family room.
`RoomFactory` creates the room matching the type column.

A malformed row stops loading with its filename and line number. A reservation refused by a
business rule is reported and skipped. `ReservationService.importExisting()` performs those
checks without sending events to the listeners.

## 11. SOLID

- **S:** CSV reading, pricing, availability, reports and menu input have separate classes.
- **O:** A new discount implements `DiscountPolicy` without changing `PricingService`.
  A new listener implements `ReservationListener` and subscribes to the service.
- **L:** Services use the same `Room` methods for standard rooms, suites and family rooms.
- **I:** The interfaces have small roles: loading data, exporting a report or applying a rule.
- **D:** Services receive repositories and other dependencies through their constructors.
  Tests can provide a fake `HotelDataSource` or `ReportExporter` and a fixed `Clock`.

## 12. Tests

```powershell
.\mvnw.cmd test
```

There are 111 JUnit 5 tests. They check bookings, pricing, cancellations, imports and reports,
including boundary cases and refused requests. CSV export tests use `@TempDir`.
Event tests use fake listeners.
