package com.dr.digital.util;

import java.util.UUID;

/**
 * uuid工具类
 *
 * @author Songxc
 */
public class UUIDUtils {

    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static Long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static String getSimpleUUID(){
        return UUID.randomUUID().toString();
    }

}
