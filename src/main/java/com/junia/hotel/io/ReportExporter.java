package com.junia.hotel.io;

import com.junia.hotel.exception.DataExportException;
import com.junia.hotel.service.Report;

import java.nio.file.Path;

public interface ReportExporter {

    void export(Report report, Path target) throws DataExportException;
}
