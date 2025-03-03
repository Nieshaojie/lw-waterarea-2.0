package com.mskyeye.trace.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @ClassName:MD5SaltUtil
 * @Description:MD5加密工具
 * @Author:R.Gong
 * @Date:2023/8/1 14:14
 * @Version:1.0
 **/
public class MD5SaltUtil {

    // 使用MD5和salt对字符串进行加密
    public static String encrypt(String input, String salt) throws NoSuchAlgorithmException {
        // 创建 MessageDigest 实例
        MessageDigest md = MessageDigest.getInstance("MD5");

        // 将密码和盐值拼接
        String saltedPassword = input + salt;

        // 执行加密
        md.update(saltedPassword.getBytes());
        byte[] hashedBytes = md.digest();

        // 转换为十六进制字符串
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();

    }
}
