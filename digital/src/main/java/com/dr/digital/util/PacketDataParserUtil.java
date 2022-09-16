package com.dr.digital.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Author: caor
 * @Date: 2021-11-25 8:42
 * @Description:
 */
public class PacketDataParserUtil {

    public static ObjectMapper objectMapper = new ObjectMapper();

    protected static Jackson2ObjectMapperBuilder createJacksonBuilder() {
        return Jackson2ObjectMapperBuilder.xml();
    }

    public static void writeData(String[] keys, Iterator<Map<String, Object>> data, String mine, OutputStream target) throws IOException {
        try {
            Jackson2ObjectMapperBuilder builder = createJacksonBuilder();
            objectMapper = builder.build();
            //反序列化的时候如果多了其他属性,不抛出异常
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            //如果是空对象的时候,不抛异常
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

            //TODO 时间戳格式化，没有效果
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            List<File> fileList = new ArrayList<>();
            for (int i = 0; i < keys.length; i++) {
                File file = new File();
                file.setId(i + 1 + "");
                file.setFilePath(keys[i]);
                fileList.add(file);
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(target, new BaseInfo(data, fileList));
        } finally {
            if (target != null) {
                target.close();
            }
            if (data instanceof Closeable) {
                ((Closeable) data).close();
            }
        }
    }

    @JacksonXmlRootElement(localName = "baseinfo")
    static class BaseInfo {

        @JacksonXmlElementWrapper(useWrapping = false)
        final Iterator<Map<String, Object>> data;

        @JacksonXmlElementWrapper(localName = "filelist")
        List<File> file;

        public BaseInfo(Iterator<Map<String, Object>> data, List<File> fileList) {
            this.data = data;
            this.file = fileList;
        }

        public Iterator<Map<String, Object>> getData() {
            return data;
        }

        public List<File> getFile() {
            return file;
        }

        public void setFile(List<File> file) {
            this.file = file;
        }
    }

    @JacksonXmlRootElement(localName = "file")
    static class File {
        @JacksonXmlProperty(isAttribute = true)
        String id;
        String filePath;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }

}