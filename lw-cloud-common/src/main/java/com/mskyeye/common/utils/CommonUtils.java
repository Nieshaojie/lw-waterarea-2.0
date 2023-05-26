package com.mskyeye.common.utils;

import java.security.MessageDigest;
import java.util.UUID;

/**
 * 工具类
 */
public class CommonUtils {

    /**
     * 产生一个32个字符的UUID（去除-）
     *
     * @return UUID
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * MD5加密工具
     * @param data
     * @return
     */
    public static String MD5(String data)  {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString().toUpperCase();
        } catch (Exception exception) {
        }
        return null;
    }
}
