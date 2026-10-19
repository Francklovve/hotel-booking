package com.junia.hotel.ui;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Scanner;

/** Reads and validates user input, asking again until the value is valid. */
public class ConsoleInput {

    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleInput(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
    }

    /** Empty when the input stream is closed (e.g. end of a piped script). */
    public Optional<String> readLine(String prompt) {
        out.print(prompt);
        if (!scanner.hasNextLine()) {
            return Optional.empty();
        }
        return Optional.of(scanner.nextLine().trim());
    }

    public Optional<Integer> readInt(String prompt) {
        while (true) {
            Optional<String> line = readLine(prompt);
            if (line.isEmpty()) {
                return Optional.empty();
            }
            try {
                return Optional.of(Integer.parseInt(line.get()));
            } catch (NumberFormatException e) {
                out.println("  Please enter a whole number.");
            }
        }
    }

    public Optional<LocalDate> readDate(String prompt) {
        while (true) {
            Optional<String> line = readLine(prompt);
            if (line.isEmpty()) {
                return Optional.empty();
            }
            try {
                return Optional.of(LocalDate.parse(line.get()));
            } catch (DateTimeParseException e) {
                out.println("  Please use the format YYYY-MM-DD, e.g. 2026-12-24.");
            }
        }
    }
}
