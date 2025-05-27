package com.mskyeye.trace.controller;

import com.mskyeye.trace.model.TraceProInfo;
import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.proc.DhCameraProc;
import com.mskyeye.trace.proc.GplCameraProc;
import com.mskyeye.trace.proc.HkCameraProc;
import com.mskyeye.trace.proc.HpCameraProc;
import com.mskyeye.trace.utils.AjaxResult;
import com.mskyeye.trace.utils.RedisCache;
import com.mskyeye.trace.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.mskyeye.trace.common.GlResources.GL_CameraInfoMap;
import static com.mskyeye.trace.common.GlResources.GL_TraceInfoMap;

/**
 * @ClassName:RCAlarmController
 * @Description:雷光警戒
 * @Author:R.Gong
 * @Date:2024/5/17 14:43
 * @Version:1.0
 **/
@RestController
@RequestMapping("/rc_alarm")
public class RCAlarmController {

    private static final Logger log = LoggerFactory.getLogger(RCAlarmController.class);
    @Autowired
    private HpCameraProc hpCameraProc;
    @Autowired
    private HkCameraProc hkCameraProc;
    @Autowired
    private DhCameraProc dhCameraProc;
    @Autowired
    private GplCameraProc gplCameraProc;
    @Autowired
    private RedisCache redisCache;



    @PostMapping("/order")
    public AjaxResult traceOrder(@RequestBody TraceProInfo traceProInfo) {
        Integer channelId = traceProInfo.getChannelId();
        Boolean bResult = false;
        try {
            YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());
            yzCameraInfo.setRcAlarmPaused(traceProInfo.getbTracking());
            log.info("设置相机是否警戒状态："+traceProInfo.getbTracking());
            redisCache.setCacheObject(yzCameraInfo.getLightCode(),channelId);
            traceProInfo.setManu(yzCameraInfo.getManu());
            if (StringUtil.isEmpty(yzCameraInfo)) {
                return AjaxResult.error("相机未登录");
            }
            for (Map.Entry<Long, TraceProInfo> entry : GL_TraceInfoMap.entrySet()) {
                TraceProInfo traceProInfo1 = entry.getValue();
                if (traceProInfo1.getCameraId() == yzCameraInfo.getId()) {
                    System.out.println("跟踪或取消跟踪信息："+traceProInfo);
                    if (traceProInfo1.getTraceType() == 5) {
                        return AjaxResult.error("请关闭该相机的AI巡航");
                    } else if (traceProInfo1.getTraceType() == 1 || traceProInfo1.getTraceType() == 2 || traceProInfo1.getTraceType() == 3) {
                        return AjaxResult.error("请关闭该相机的跟踪");
                    }
                }
            }
            if(traceProInfo.getbTracking() == true){
                TraceProInfo newTraceProInfo = new TraceProInfo();
                newTraceProInfo = traceProInfo;
                GL_TraceInfoMap.put(newTraceProInfo.getCameraId(), newTraceProInfo);
            }else {
                GL_TraceInfoMap.remove(traceProInfo.getCameraId());
            }
            return AjaxResult.success();

        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error();
        }
    }
}
