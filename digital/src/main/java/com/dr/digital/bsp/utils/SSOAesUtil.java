package com.dr.digital.bsp.utils;

import org.bouncycastle.util.encoders.UrlBase64Encoder;
import org.springframework.util.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * aes对称加密工具类
 * <p>
 * 这里的代码只是针对浪潮sso代码实现的，别的业务逻辑不要轻易使用
 *
 * @author dr
 */
public class SSOAesUtil {

    public static String decodeToString(String encodeString, String secretKey, Charset charsetName) throws Exception {
        //key先MD5
        byte[] secBytes = DigestUtils.md5Digest(secretKey.getBytes(charsetName));
        //获取key
        SecretKeySpec keySpec = new SecretKeySpec(secBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        UrlBase64Encoder encoder = new UrlBase64Encoder();
        byte[] bytes = encodeString.getBytes(charsetName);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        encoder.decode(bytes, 0, bytes.length, stream);
        return new String(cipher.doFinal(stream.toByteArray()));
    }

    public static String decodeToString(String encodeString, String secretKey) throws Exception {
        return decodeToString(encodeString, secretKey, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        String secretKey = "inspurh2wmABdfM7i3K80mAS";
        String encodingStr = "MWlqkrsDmyPJ9Ta8gB298Em3vUiQ7m-0PSh7luXcvF0deI99TA-lHcikMpF1Seu_HwJjFmfJy7ppy4TN4J8IwAAWO_eYtTAivbqti_ukBNJjyvPihwlVP0QP7lnP2Df1t7WYEqOUBdsVz5K137I7BCG2tWdIxov-6rvjSrCD3aPpZ-_kRmUIkJo9_Y8s015R";
        System.out.println(decodeToString(encodingStr, secretKey));
    }

}
