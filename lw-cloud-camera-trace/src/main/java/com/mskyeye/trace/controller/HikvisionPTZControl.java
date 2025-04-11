package com.mskyeye.trace.controller;

import com.mskyeye.trace.camera.hkws.sdk.HkNetSDK;
import com.mskyeye.trace.camera.hkws.struct.NET_DVR_DEVICEINFO_V40;
import com.mskyeye.trace.camera.hkws.struct.NET_DVR_USER_LOGIN_INFO;
import com.sun.jna.Native;

public class HikvisionPTZControl {
    static HkNetSDK HkNetSDK = (HkNetSDK) Native.loadLibrary("win32-x86-64/hk_lib/win/HCNetSDK.dll", HkNetSDK.class);
    private static int userId = -1;

    public static boolean initSDK() {
        boolean result = HkNetSDK.NET_DVR_Init();
        if (!result) {
            System.out.println("SDK 初始化失败，错误码：" + HkNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("SDK 初始化成功！");
        }
        return result;
    }

    public static int loginDevice(String ip, String user, String password) {
        NET_DVR_USER_LOGIN_INFO loginInfo = new NET_DVR_USER_LOGIN_INFO();
        NET_DVR_DEVICEINFO_V40 deviceInfo = new NET_DVR_DEVICEINFO_V40();

        System.arraycopy(ip.getBytes(), 0, loginInfo.sDeviceAddress, 0, ip.length());
        System.arraycopy(user.getBytes(), 0, loginInfo.sUserName, 0, user.length());
        System.arraycopy(password.getBytes(), 0, loginInfo.sPassword, 0, password.length());

        loginInfo.wPort = (short) 8000;
//        loginInfo.byUseAsynLogin = 0; // 同步登录

         userId = HkNetSDK.NET_DVR_Login_V40(loginInfo, deviceInfo);

//        int userId = nativeLongValue.intValue();
        if (userId < 0) {
            System.out.println("登录失败，错误码：" + HkNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("登录成功，用户ID：" + userId);
        }
        return userId;
    }

    public static void controlPTZ(int command) {
        if (userId < 0) {
            System.out.println("❌ 请先登录设备！");
            return;
        }

        // 发送开始移动命令
        HkNetSDK.NET_DVR_PTZControl_Other(userId, 0, command, 0);
        HkNetSDK.NET_DVR_PTZControl_Other(userId, 0, command, 0);
        HkNetSDK.NET_DVR_PTZControl_Other(userId, 0, command, 0);
        HkNetSDK.NET_DVR_PTZControl_Other(userId, 0, command, 0);
        boolean result = HkNetSDK.NET_DVR_PTZControl_Other(userId, 1, command, 0);
        if (!result) {
            System.out.println("❌ 云台控制失败，错误码：" + HkNetSDK.NET_DVR_GetLastError());
            return;
        }
        System.out.println("✅ 云台控制开始");

        // 给云台一些时间来移动（例如，控制 3 秒钟）
        try {
            Thread.sleep(3000);  // 控制时间，单位毫秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 发送停止命令
        result = HkNetSDK.NET_DVR_PTZControl_Other( userId,1, command, 1);  // 停止操作
        if (!result) {
            System.out.println("❌ 云台停止失败，错误码：" + HkNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("✅ 云台停止成功");
        }
    }


    /*public static void logout() {
        if (userId >= 0) {
            HkNetSDK.NET_DVR_Logout(userId);
        }
        HkNetSDK.NET_DVR_Cleanup();
    }*/

    public static void main(String[] args) {
        if (!initSDK()) {
            System.out.println("SDK 初始化失败");
            return;
        }
        int user = loginDevice("103.48.232.122", "admin", "abcd1234");
        if (user >= 0) {
            controlPTZ(0x0004);// 左移命令
        }
//        logout();
    }
}
