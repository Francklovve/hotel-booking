package com.junia.hotel.io;

import com.junia.hotel.exception.DataLoadingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads simple CSV files (no quoted fields). Skips the header, blank lines and "#" comments.
 * Files are read from a directory if one is given, otherwise from the classpath (src/main/resources).
 */
class CsvFileReader {

    private static final String CLASSPATH_FOLDER = "/data/";

    private final Path directory;

    CsvFileReader(Path directory) {
        this.directory = directory;
    }

    List<CsvLine> read(String fileName, int expectedColumns) throws DataLoadingException {
        try (BufferedReader reader = open(fileName)) {
            List<CsvLine> lines = new ArrayList<>();
            String line;
            int lineNumber = 0;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                // -1 keeps trailing empty columns, e.g. "101,STANDARD,2,80,"
                String[] fields = line.split(",", -1);
                if (fields.length < expectedColumns) {
                    throw new DataLoadingException(fileName + " line " + lineNumber + ": expected "
                            + expectedColumns + " columns but found " + fields.length);
                }
                lines.add(new CsvLine(fileName, lineNumber, fields));
            }
            return lines;
        } catch (IOException e) {
            throw new DataLoadingException("Unable to read " + fileName + ": " + e.getMessage(), e);
        }
    }

    private BufferedReader open(String fileName) throws IOException, DataLoadingException {
        if (directory != null) {
            Path path = directory.resolve(fileName);
            if (!Files.exists(path)) {
                throw new DataLoadingException("Data file not found: " + path.toAbsolutePath());
            }
            return Files.newBufferedReader(path, StandardCharsets.UTF_8);
        }
        InputStream stream = CsvFileReader.class.getResourceAsStream(CLASSPATH_FOLDER + fileName);
        if (stream == null) {
            throw new DataLoadingException("Data file not found on classpath: " + CLASSPATH_FOLDER + fileName);
        }
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
}
