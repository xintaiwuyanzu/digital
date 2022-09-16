package com.dr.digital.manage.impexpscheme.service.imp;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * 借助fastxml实现数据解析，对应json和xml
 *
 * @author dr
 */
@Component
public class XmlDataParser extends AbstractObjectMapperDataParser {
    @Override
    public String getFileSuffix(String mine) {
        return "xml";
    }

    @Override
    public boolean canHandle(String mine) {
        return MediaType.APPLICATION_XML.includes(MediaType.parseMediaType(mine));
    }

    @Override
    protected Jackson2ObjectMapperBuilder createJacksonBuilder() {
        return Jackson2ObjectMapperBuilder.xml();
    }

    @Override
    public void writeData(String[] keys, Iterator<Map<String, Object>> data, String mine, OutputStream target) throws IOException {
        try {
            objectMapper.writeValue(target, new Archive(data));
        } finally {
            if (target != null) {
                target.close();
            }
            if (data instanceof Closeable) {
                ((Closeable) data).close();
            }
        }
    }

    static class Archive {
        final Iterator<Map<String, Object>> data;

        public Archive(Iterator<Map<String, Object>> data) {
            this.data = data;
        }

        public Iterator<Map<String, Object>> getData() {
            return data;
        }
    }

}
