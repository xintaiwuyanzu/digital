package com.dr.digital.manage.impexpscheme.service.imp;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

/**
 * 借助fastxml实现数据解析，对应json和xml
 *
 * @author dr
 */
@Component
public class JsonDataParser extends AbstractObjectMapperDataParser {
    @Override
    public String getFileSuffix(String mine) {
        return "json";
    }

    @Override
    public boolean canHandle(String mine) {
        return MediaType.APPLICATION_JSON.includes(MediaType.parseMediaType(mine));
    }

    @Override
    protected Jackson2ObjectMapperBuilder createJacksonBuilder() {
        return Jackson2ObjectMapperBuilder.json();
    }
}
