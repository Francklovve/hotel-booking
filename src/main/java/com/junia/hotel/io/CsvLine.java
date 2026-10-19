package com.junia.hotel.io;

/** One data line of a CSV file, with its line number to produce helpful error messages. */
record CsvLine(String fileName, int lineNumber, String[] fields) {

    String field(int index) {
        return index < fields.length ? fields[index].trim() : "";
    }

    String location() {
        return fileName + " line " + lineNumber;
    }
}
