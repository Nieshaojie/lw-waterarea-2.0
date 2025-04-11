package com.mskyeye.trace.utils;

import com.mskyeye.trace.camera.hkws.sdk.HkNetSDK;
import com.sun.jna.Native;

public class HkNetSDKLoader {
    private static final String WINDOWS_PATH = "win32-x86-64/hk_lib/win/HCNetSDK.dll";
    private static final String LINUX_PATH = "/home/hk_lib/linux/libhcnetsdk.so";

    public static HkNetSDK loadSDK() {
        String os = System.getProperty("os.name").toLowerCase();
        String url;

        if (os.contains("win")) {
            url = WINDOWS_PATH;
        } else if (os.contains("linux")) {
            url = LINUX_PATH;
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }

        return Native.load(url, HkNetSDK.class);
    }

}
