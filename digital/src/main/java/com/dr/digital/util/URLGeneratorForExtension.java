package com.dr.digital.util;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Title: GenerateWebOFDCodeMk2
 * @Description 生成通用第三方访问云阅读的路径
 * @Author zhaoJing
 * @Date Create on 2021/6/2 16:56
 */
public class URLGeneratorForExtension {

    private static String fixedAESPassword = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCSfP+4H+XcjTZvDyJC98Ve9Mdr5vSOfE5AMwyZIwBYy3BtrlnUPgQI14ciiuVOTJkxSF0yKoftrw15M7xUMz8HERwfVHGjtfr40q0hCh3G/vnIOX7FdzuAnvqomC+q3MpPYCraNT79Ugz2+5NGrs0ukP7ZzW31+lmsIXvMZZre+wIDAQAB";

    /* 生成一个访问地址 */
    public static String generateWebOFDUrl(String baseAddress, String docParam, WebOFDBizTypeEnum bizTypeEnum, String extensionApiMode) {
        StringBuilder result = new StringBuilder(baseAddress + "?docParam=");
        switch (bizTypeEnum) {
            case restfulsingleparam:
                result.append(docParam);
                result.append("&paramType=restfulsingleparam");
                break;
            case restfulmultiparam:
                result.append(encodeWebOFDDocParam(docParam));
                result.append("&paramType=restfulmultiparam");
                break;
            case restfuldm:
                result.append(encodeWebOFDDocParam(docParam));
                result.append("&paramType=restfuldm");
                break;
        }
        if (isNotBlank(extensionApiMode)) {
            result.append("&linkip=" + extensionApiMode);
        }
        return result.toString();
    }

    public static String encodeWebOFDDocParam(String json) {
        return encryptAES(json, fixedAESPassword);
    }

    public static String decodeWebOFDDocParam(String sourceString) {
        return decrypt(sourceString, fixedAESPassword);
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public enum WebOFDBizTypeEnum {
        restfulsingleparam, restfulmultiparam, restfuldm;

        WebOFDBizTypeEnum() {
        }
    }



    /* ------------------------------------------------------------AES工具方法-------------------------------------------------------- */

    /**
     * AES加密
     *
     * @param content
     * @param password
     * @return
     */
    public static String encryptAES(String content, String password) {
        if (IsNullOrEmpty(content))
            return content;
        byte[] encryptResult = encrypt(content, password);
        String encryptResultStr = parseByte2HexStr(encryptResult);
        return encryptResultStr;
    }

    /**
     * AES解密
     *
     * @param encryptResultStr
     * @param password
     * @return
     */
    public static String decrypt(String encryptResultStr, String password) {
        /* modified by zhaojing on 2020209. add a string filter.Fixed a formatting error caused by line breaks */
        encryptResultStr = replaceSpecialStr(encryptResultStr);
        if (IsNullOrEmpty(encryptResultStr))
            return encryptResultStr;
        byte[] decryptFrom = parseHexStr2Byte(encryptResultStr);
        byte[] decryptResult = decrypt(decryptFrom, password);
        return new String(decryptResult);
    }


    /**
     * 加密
     *
     * @param content  需要加密的内容
     * @param password 加密密码
     * @return
     */
    private static byte[] encrypt(String content, String password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            // 防止linux下 随机生成key
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(password.getBytes());
            kgen.init(128, secureRandom);
            // kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e);
        } catch (NoSuchPaddingException e) {
            System.err.println(e);
        } catch (InvalidKeyException e) {
            System.err.println(e);
        } catch (UnsupportedEncodingException e) {
            System.err.println(e);
        } catch (IllegalBlockSizeException e) {
            System.err.println(e);
        } catch (BadPaddingException e) {
            System.err.println(e);
        }
        return null;
    }

    /**
     * 解密
     *
     * @param content  待解密内容
     * @param password 解密密钥
     * @return
     */
    private static byte[] decrypt(byte[] content, String password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            // 防止linux下 随机生成key
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(password.getBytes());
            kgen.init(128, secureRandom);
            // kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(content);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e);
        } catch (NoSuchPaddingException e) {
            System.err.println(e);
        } catch (InvalidKeyException e) {
            System.err.println(e);
        } catch (IllegalBlockSizeException e) {
            System.err.println(e);
        } catch (BadPaddingException e) {
            System.err.println(e);
        }
        return null;
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
                    16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    public static boolean IsNullOrEmpty(String strValue) {
        if (strValue == null || strValue.isEmpty())
            return true;
        else
            return false;
    }


    /**
     * 去除字符串中的空格、回车、换行符、制表符等
     * added by zhaojing on 20200927
     *
     * @param str
     * @return
     */
    private static String replaceSpecialStr(String str) {
        String repl = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            repl = m.replaceAll("");
        }
        return repl;
    }
    /* ------------------------------------------------------------AES工具方法-------------------------------------------------------- */


}
