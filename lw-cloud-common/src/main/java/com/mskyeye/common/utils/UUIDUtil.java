package com.mskyeye.common.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

/**
 * @Author R.Gong
 * @Description //程序工具类
 * @Date 14:30 2020/7/16
 * @Param
 * @return
 **/
public class UUIDUtil {

    /**
     * 产生一个32个字符的UUID（去除-）
     *
     * @return UUID
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * md5加密
     *
     * @param value 要加密的值
     * @return md5加密后的值
     */
    public static String md5Hex(String value) {
        return DigestUtils.md5Hex(value);
    }

    /**
     * sha1加密
     *
     * @param value 要加密的值
     * @return sha1加密后的值
     */
    public static String sha1Hex(String value) {
        return DigestUtils.sha1Hex(value);
    }

    /**
     * sha256加密
     *
     * @param value 要加密的值
     * @return sha256加密后的值
     */
    public static String sha256Hex(String value) {
        return DigestUtils.sha256Hex(value);
    }

}
