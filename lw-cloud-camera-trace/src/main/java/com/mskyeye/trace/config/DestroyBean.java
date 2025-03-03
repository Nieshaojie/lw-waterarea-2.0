package com.mskyeye.trace.config;

import com.mskyeye.trace.proc.HpCameraProc;
import com.mskyeye.trace.model.YzCameraInfo;
import com.sun.jna.NativeLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;

import static com.mskyeye.trace.common.GlResources.GL_CameraInfoMap;

/**
 * @ClassName:DestroyBean
 * @Description:相机登出操作
 * @Author:R.Gong
 * @Date:2023/8/8 13:44
 * @Version:1.0
 **/
@Component
public class DestroyBean {

    @Autowired
    private HpCameraProc hpCameraProc;

    @PreDestroy
    public void onDestroy() throws Exception {
        // 在应用程序结束时调用的方法
        for (Map.Entry<Long, YzCameraInfo> entry : GL_CameraInfoMap.entrySet()) {
            YzCameraInfo yzCameraInfo = entry.getValue();
            if (yzCameraInfo.getManu().equals("hp")) {
                hpCameraProc.userLogout(yzCameraInfo);
            } else if (yzCameraInfo.getManu().equals("hik")) {
                yzCameraInfo.getHkNetSDK().NET_DVR_Logout(new NativeLong(Long.valueOf(yzCameraInfo.getLoginInfo())));
                yzCameraInfo.getHkNetSDK().NET_DVR_Cleanup();
                //关闭布防上传通道
                if(yzCameraInfo.getlHandle() >= 0){
                    yzCameraInfo.getHkNetSDK().NET_DVR_CloseAlarmChan_V30(yzCameraInfo.getlHandle());
                }
            } else if (yzCameraInfo.getManu().equals("dh")) {
                yzCameraInfo.getDhNetSDK().CLIENT_Logout(Long.valueOf(yzCameraInfo.getLoginInfo()));
                yzCameraInfo.getDhNetSDK().CLIENT_Cleanup();
            }
        }
    }
}
