package com.dr.digital.manage.impexpscheme.service.imp;

import com.dr.digital.manage.impexpscheme.service.DataParser;
import com.linuxense.javadbf.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * DBF操作类
 *
 * @author dr
 */
@Component
public class DBFDataParser implements DataParser {
    final MediaType mediaType = MediaType.parseMediaType("application/x-dbf");

    @Override
    public boolean canHandle(String mine) {
        return mediaType.includes(MediaType.parseMediaType(mine));
    }

    @Override
    public String getFileSuffix(String mine) {
        return "dbf";
    }

    @Override
    public String[] readKeys(InputStream source, String mine) throws DBFException {
        DBFReader dbfReader = new DBFReader(source, Charset.forName("GBK"));
        String[] keys = doReadKeys(dbfReader);
        dbfReader.close();
        return keys;
    }

    @Override
    public Iterator<Map<String, Object>> readData(InputStream source, String mine) throws DBFException {
        Charset charset = Charset.forName("GBK");
        DBFReader dbfReader = new DBFReader(source, charset);
        String[] keys = doReadKeys(dbfReader);
        return new DbfRowIterator(dbfReader, keys);
    }

    static class DbfRowIterator implements Iterator<Map<String, Object>>, Closeable {
        final DBFReader dbfReader;
        final String[] keys;
        final int count;
        int index = 0;

        DbfRowIterator(DBFReader dbfReader, String[] keys) {
            this.dbfReader = dbfReader;
            this.keys = keys;
            count = dbfReader.getRecordCount();
        }

        @Override
        public boolean hasNext() {
            return index < count;
        }

        @Override
        public Map<String, Object> next() {
            index++;
            DBFRow row = null;
            try {
                row = dbfReader.nextRow();
            } catch (DBFException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Object> data = new HashMap<>(keys.length);
            for (String key : keys) {
                String value = row.getString(key);
                data.put(key, value);
            }
            return data;
        }

        @Override
        public void close() {
            dbfReader.close();
        }
    }

    protected String[] doReadKeys(DBFReader dbfReader) throws DBFException {
        Set<String> strings = new HashSet<>();
        int fieldCount = dbfReader.getFieldCount();
        for (int i = 0; i < fieldCount; i++) {
            DBFField field = dbfReader.getField(i);
            strings.add(field.getName());
        }
        return strings.toArray(new String[0]);
    }

    @Override
    public void writeData(String[] keys, Iterator<Map<String, Object>> data, String mine, OutputStream target) throws DBFException {
        //TODO写DBF暂时没用到，代码需要重写
        /*DBFWriter dbfWriter = new DBFWriter(target, Charset.forName("GBK"));
        dbfWriter.setFields(Arrays.stream(keys).map(this::newField).toArray(DBFField[]::new));
        while (data.hasNext()) {
            Map<String, Object> map = data.next();
            String[] objects = Arrays.stream(keys)
                    .map(k -> map.getOrDefault(k, ""))
                    .map(v -> v == null ? "" : v.toString())
                    .toArray(String[]::new);
            dbfWriter.addRecord(objects);
        }
        dbfWriter.close();*/
    }



}
