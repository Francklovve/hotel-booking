package com.junia.hotel.io;

record CsvLine(String fileName, int lineNumber, String[] fields) {

    String field(int index) {
        return index < fields.length ? fields[index].trim() : "";
    }

    String location() {
        return fileName + " line " + lineNumber;
    }
}
