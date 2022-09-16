package com.dr.digital.manage.impexpscheme.service.imp;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author dr
 */
@Component
public class ExcelDataParser extends AbstractExcelDataParser {
    final MediaType mediaType = MediaType.parseMediaType("application/vnd.ms-excel");

    @Override
    public String getFileSuffix(String mine) {
        return "xls";
    }

    @Override
    public boolean canHandle(String mine) {
        return mediaType.includes(MediaType.parseMediaType(mine));
    }

    @Override
    protected Workbook createWorkBook(InputStream source) throws IOException {
        if (source == null) {
            return new HSSFWorkbook();
        } else {
            return new HSSFWorkbook(source);
        }
    }
}
