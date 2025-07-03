package com.mskyeye.trace.camera.gpl.sdk;

import com.mskyeye.trace.camera.gpl.callback.HaveReConnect;
import com.mskyeye.trace.camera.gpl.callback.fDisConnectCB;
import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.utils.StringUtil;
import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class GplSdkManager {

    private static final fDisConnectCB DISCONNECT_CB = new fDisConnectCB();
    private static final HaveReConnect RECONNECT_CB = new HaveReConnect();
    private static final AtomicBoolean SDK_INITIALIZED = new AtomicBoolean(false);
    private static GplNetSDK sdk;

    // 存储所有成功登录的句柄
    private static final List<Long> loginHandles = Collections.synchronizedList(new ArrayList<>());

    static {
        // JVM 退出时统一释放资源
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🧹 JVM 退出，释放 GPL SDK 资源...");
            if (sdk != null) {
                synchronized (loginHandles) {
                    for (Long handle : loginHandles) {
                        if (handle != null && handle != 0L) {
                            try {
                                sdk.VSIF_Logout(handle);
                            } catch (Exception e) {
                                System.err.println("❌ 登出句柄失败: " + e.getMessage());
                            }
                        }
                    }
                    loginHandles.clear();
                }

                try {
                    sdk.VSIF_Cleanup(); // ⚠️ 释放SDK内存资源
                    System.out.println("✅ SDK Cleanup 完成");
                } catch (Exception e) {
                    System.err.println("❌ SDK Cleanup 异常: " + e.getMessage());
                }
            }
        }));
    }

    public static synchronized GplNetSDK getInstance() {
        if (sdk == null) {
            Map<String, Object> options = new HashMap<>();
            options.put(Library.OPTION_CALLING_CONVENTION, Function.C_CONVENTION);
            sdk = Native.load("/home/hk_lib/linux/linuxSDKdemo/m64/libvsifsdk.so", GplNetSDK.class, options);
        }

        if (SDK_INITIALIZED.compareAndSet(false, true)) {
            boolean success = sdk.VSIF_Init(DISCONNECT_CB, new NativeLong(0));
            if (!success) {
                throw new RuntimeException("SDK 初始化失败，错误码: 0x" + Integer.toHexString(sdk.VSIF_GetLastError()));
            }
            sdk.VSIF_SetAutoReconnect(RECONNECT_CB, new NativeLong(0));
            sdk.VSIF_SetConnectTime(5000, 3);
        }

        return sdk;
    }

    /**
     * 检查当前登录是否有效
     */
    public static boolean isLoginValid(String loginInfo) {
        try {
            if (StringUtil.isEmpty(loginInfo)) return false;
            long handle = Long.parseLong(loginInfo);
            IntByReference ref = new IntByReference();
            boolean result = getInstance().VSIF_GetLoginState(new NativeLong(handle), ref);
            return result && ref.getValue() == 1;
        } catch (Throwable t) {
            return false;
        }
    }


    /**
     * 登录并返回句柄
     */
    public static synchronized long login(YzCameraInfo cameraInfo) {
        GplNetSDK sdk = getInstance();

        int[] nError = new int[1];
        Pointer pSpecCap = Pointer.NULL;

        long loginId = sdk.VSIF_LoginEx2(
                cameraInfo.getIp1(),
                (short) cameraInfo.getManPort().intValue(),
                cameraInfo.getUserName(),
                cameraInfo.getPassWord(),
                0, pSpecCap, 0, nError);

        if (loginId == 0) {
            int err = sdk.VSIF_GetLastError();
            System.err.printf("❌ GPL 登录失败: loginId=0, 错误码=0x%x, nError=%d%n", err, nError[0]);
            throw new RuntimeException("GPL 登录失败");
        }

        // 避免重复添加
        synchronized (loginHandles) {
            if (!loginHandles.contains(loginId)) {
                loginHandles.add(loginId);
            }
        }

        return loginId;
    }

    public static void removeHandle(Long handle) {
        if (handle != null && handle != 0L) {
            synchronized (loginHandles) {
                loginHandles.remove(handle);
            }
        }
    }

}