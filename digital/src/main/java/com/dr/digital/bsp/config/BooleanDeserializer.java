package com.dr.digital.bsp.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * 用来将字符串转换成boolean类型
 * 0是false
 * 1是true
 *
 * @author dr
 */
public class BooleanDeserializer extends JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return "1".equals(p.getText());
    }
}
