package com.dr.digital.manage.impexpscheme.service.imp;

import com.dr.digital.manage.impexpscheme.service.DataParser;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * 借助fastxml实现数据解析，对应json和xml
 *
 * @author dr
 */
public abstract class AbstractObjectMapperDataParser implements DataParser, ApplicationContextAware, InitializingBean {
    protected ObjectMapper objectMapper;
    protected ApplicationContext applicationContext;


    @Override
    public String[] readKeys(InputStream source, String mine) throws IOException {
        Set<String> strings = new HashSet<>();
        try (JsonParser jsonParser = objectMapper.createParser(source)) {
            while (!jsonParser.isClosed()) {
                String fieldName = jsonParser.nextFieldName();
                if (StringUtils.isEmpty(fieldName)) {
                    continue;
                }
                if (strings.contains(fieldName)) {
                    break;
                }
                strings.add(fieldName);
            }
        }
        return strings.toArray(new String[0]);
    }

    @Override
    public Iterator<Map<String, Object>> readData(InputStream source, String mine) throws IOException {
        //TODO 数据全到内存了，有性能问题
        JavaType jt = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, Map.class);
        List<Map<String, Object>> mapList = objectMapper.readValue(source, jt);
        return mapList.iterator();
    }

    @Override
    public void writeData(String[] keys, Iterator<Map<String, Object>> data, String mine, OutputStream target) throws IOException {
        try {
            objectMapper.writeValue(target, data);
        } finally {
            if (target != null) {
                target.close();
            }
            if (data instanceof Closeable) {
                ((Closeable) data).close();
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 初始化objectMapper
     */
    @Override
    public void afterPropertiesSet() {
        Jackson2ObjectMapperBuilder builder = createJacksonBuilder();
        builder.applicationContext(applicationContext);
        objectMapper = builder.build();
    }

    protected abstract Jackson2ObjectMapperBuilder createJacksonBuilder();

}
