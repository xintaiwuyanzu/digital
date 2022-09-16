package com.dr.digital.manage.impexpscheme.service.imp;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author dr
 */
@Component
public class XExcelDataParser extends AbstractExcelDataParser {
    final MediaType mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    @Override
    public String getFileSuffix(String mine) {
        return "xlsx";
    }

    @Override
    public boolean canHandle(String mine) {
        return mediaType.includes(MediaType.parseMediaType(mine));
    }

    @Override
    protected Workbook createWorkBook(InputStream source) throws IOException {
        if (source == null) {
            return new XSSFWorkbook();
        }
        return new XSSFWorkbook(source);
    }
}
