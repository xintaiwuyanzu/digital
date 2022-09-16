package com.dr.digital.manage.impexpscheme.service.imp;

import com.dr.digital.manage.impexpscheme.service.DataParser;
import org.apache.poi.ss.usermodel.*;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 解析excel
 *
 * @author dr
 */
public abstract class AbstractExcelDataParser implements DataParser {
    @Override
    public String[] readKeys(InputStream source, String mine) throws IOException {
        //创建book
        Workbook workbook = createWorkBook(source);
        //读取第一个sheet
        Sheet sheet = workbook.getSheetAt(0);
        //读取第一行数据
        Row row = sheet.getRow(sheet.getFirstRowNum());
        //遍历第一行所有数据
        Iterator<Cell> cellIterator = row.cellIterator();
        Set<String> stringSet = new HashSet<>();

        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            if (cell != null) {
                String value = readStringValue(cell);
                if (!StringUtils.isEmpty(value)) {
                    stringSet.add(value.trim());
                }
            }
        }
        return stringSet.toArray(new String[0]);
    }


    /**
     * 创建poi excel对象
     *
     * @param source
     * @return
     */
    protected abstract Workbook createWorkBook(InputStream source) throws IOException;

    @Override
    public Iterator<Map<String, Object>> readData(InputStream source, String mine) throws IOException {
        //创建book
        Workbook workbook = createWorkBook(source);
        //读取第一个sheet
        Sheet sheet = workbook.getSheetAt(0);
        //读取第一行数据
        Row row = sheet.getRow(sheet.getFirstRowNum());
        //
        Iterator<Row> rowIterator = sheet.rowIterator();
        return new PoiRowIterator(row, rowIterator, workbook);
    }

    @Override
    public void writeData(String[] keys, Iterator<Map<String, Object>> data, String mine, OutputStream target) {
        try (Workbook workbook = createWorkBook(null)) {
            Sheet sheet = workbook.createSheet();
            //先创建第一行头
            int rowCount = 0;
            Row header = sheet.createRow(rowCount);
            for (int i = 0; i < keys.length; i++) {
                header.createCell(i).setCellValue(keys[i]);
            }
            while (data.hasNext()) {
                rowCount++;
                Map<String, Object> map1 = data.next();
                Row row = sheet.createRow(rowCount);
                for (int i = 0; i < keys.length; i++) {
                    Object v = map1.get(keys[i]);
                    row.createCell(i).setCellValue(v == null ? "" : v.toString());
                }
            }
            workbook.write(target);
        } catch (Exception ignored) {
        }
    }

    class PoiRowIterator implements Iterator<Map<String, Object>>, Closeable {
        final Iterator<Row> iterator;
        final Workbook workbook;
        final List<String> headers;


        public PoiRowIterator(Row first, Iterator<Row> iterator, Workbook workbook) {
            //去掉第一行
            iterator.next();
            this.iterator = iterator;
            this.workbook = workbook;
            headers = new ArrayList<>();
            Iterator<Cell> cellIterator = first.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                String key = readStringValue(cell);
                headers.add(key);
            }
        }

        @Override
        public void close() throws IOException {
            workbook.close();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Map<String, Object> next() {
            Row row = iterator.next();
            Map<String, Object> map = new HashMap<>(row.getRowNum());
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    String value = readStringValue(cell);
                    if (!StringUtils.isEmpty(value)) {
                        map.putIfAbsent(headers.get(i), value);
                    }
                }
            }
            return map;
        }
    }

    protected String readStringValue(Cell cell) {
        CellType cellType = cell.getCellType();
        String value = null;
        switch (cellType) {
            case STRING:
                value = cell.getStringCellValue();
                break;
            case _NONE:
            case ERROR:
            case BLANK:
                break;
            case NUMERIC:
                DecimalFormat df = new DecimalFormat("0");
                value = df.format(cell.getNumericCellValue());
//                value = String.valueOf(cell.getNumericCellValue());
                break;
            default:
                try {
                    value = cell.getStringCellValue();
                } catch (Exception ignored) {
                }
                break;
        }
        return value;
    }

}
