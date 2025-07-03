package com.mskyeye.trace.camera.gpl.sdk;

import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.utils.StringUtil;

import java.util.function.Function;

import static com.mskyeye.trace.common.GlResources.GL_CameraInfoMap;

public class GplControlHelper {

    /**
     * 执行带自动登录校验的控制操作
     */
    public static boolean execute(YzCameraInfo info, Function<YzCameraInfo, Boolean> action) {
        long loginId = -1;
        boolean tempLogin = false;

        try {
            if (info == null || !"gpl".equals(info.getManu())) return false;

            GplNetSDK sdk = GplSdkManager.getInstance();

            // 登录（临时）
            if (StringUtil.isEmpty(info.getLoginInfo()) || "null".equalsIgnoreCase(info.getLoginInfo())) {
                loginId = GplSdkManager.login(info);
                info.setLoginInfo(String.valueOf(loginId));
                info.setGplNetSDK(sdk);
                tempLogin = true;
            }

            return action.apply(info);

        } catch (Exception e) {
            System.err.println("❌ 控制命令执行失败：" + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // 仅对临时登录做登出，避免误删全局登录
            if (tempLogin && !StringUtil.isEmpty(info.getLoginInfo())) {
                try {
                    long handle = Long.parseLong(info.getLoginInfo());
                    boolean b = GplSdkManager.getInstance().VSIF_Logout(handle);
                    System.out.println("🧹 控制后登出句柄成功：" + b);
                    GplSdkManager.removeHandle(handle); // 从全局 handle 列表移除
                } catch (Exception ex) {
                    System.err.println("⚠️ 登出句柄异常：" + ex.getMessage());
                }
                info.setLoginInfo(null);
            }
        }
    }


}