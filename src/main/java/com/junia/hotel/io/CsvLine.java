package com.junia.hotel.io;

/** Garde le numéro de ligne pour retrouver une erreur dans le CSV. */
record CsvLine(String fileName, int lineNumber, String[] fields) {

    String field(int index) {
        return index < fields.length ? fields[index].trim() : "";
    }

    String location() {
        return fileName + " line " + lineNumber;
    }
}
