package com.dr.digital.ocr.service.impl;

import com.dr.digital.ocr.OcrConfig;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * @author caor
 * @date 2021-09-18 16:43
 */
@Configuration
public class FeignClientTemplateConfig implements RequestInterceptor {
    Logger logger = LoggerFactory.getLogger(FeignClientTemplateConfig.class);
    @Autowired
    OcrConfig ocrConfig;

    @Override
    public void apply(RequestTemplate template) {
        String nonce = UUID.randomUUID().toString();
        String timestamp = getTimestamp();
        template.header("Api-Auth-pubkey", ocrConfig.getTemplate_pubkey());
        template.header("Api-Auth-timestamp", timestamp);
        template.header("Api-Auth-nonce", nonce);
        template.header("Api-Auth-sign", getSign(nonce, timestamp));
        logger.info("Template-header:" + template.headers());
    }

    /**
     * 获取秒的时间戳
     *
     * @return
     */
    String getTimestamp() {
        return (System.currentTimeMillis() + "").substring(0, (System.currentTimeMillis() + "").length() - 3);
    }

    /**
     * @param inStr
     * @return
     * @throws Exception
     */
    public String shaEncode(String inStr) throws Exception {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA");
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            return "";
        }

        byte[] byteArray = inStr.getBytes(StandardCharsets.UTF_8);
        byte[] md5Bytes = sha.digest(byteArray);
        StringBuilder hexValue = new StringBuilder();
        for (byte md5Byte : md5Bytes) {
            int val = ((int) md5Byte) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    String getSign(String nonce, String timestamp) {
        //生成随机[a-z]字符串，包含大小写
        try {
            return shaEncode(nonce + timestamp + ocrConfig.getTemplate_secret_key());
        } catch (Exception e) {
            e.printStackTrace();
            return "获取签名失败";
        }
    }
}
