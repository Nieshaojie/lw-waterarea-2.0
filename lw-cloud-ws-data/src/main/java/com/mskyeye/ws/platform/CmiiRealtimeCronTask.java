package com.mskyeye.ws.platform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mskyeye.ws.utils.WebSocketSession;
import com.mskyeye.ws.platform.DroneVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 无人机/机库实时数据定时推送任务。
 * 机库及绑定无人机清单每 5 分钟通过 get_dock_drone_sn 接口动态刷新并缓存，
 * 每 5 秒基于缓存清单调用第三方实时接口，将结果通过 WebSocket 广播给所有前端。
 * 一个机库可绑定多架无人机，逐一推送每架无人机实时数据。
 * 前端消息体：{type, sn, data}，type 取值 DOCK_REALTIME / DRONE_REALTIME。
 *
 * @author mskyeye
 */
@Component
public class CmiiRealtimeCronTask {

    private static final Logger log = LoggerFactory.getLogger(CmiiRealtimeCronTask.class);

    private static final String TYPE_DOCK = "DOCK_REALTIME";
    private static final String TYPE_DRONE = "DRONE_REALTIME";

    /** 机库/无人机清单刷新周期：5 分钟 */
    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000L;

    @Autowired
    private CmiiProperties props;

    @Autowired
    private CmiiRealtimeClient client;

    /** 机库及绑定无人机清单缓存 */
    private volatile List<DockDroneVO> dockDroneList = new ArrayList<>();

    /**
     * 每 5 分钟刷新一次机库/无人机清单
     */
    @Scheduled(fixedRate = REFRESH_INTERVAL_MS, initialDelay = 3000)
    public void refreshDockDroneList() {
        try {
            List<DockDroneVO> list = client.getDockDroneList();
            dockDroneList = list;
            log.info("刷新机库/无人机清单成功，共 {} 个机库", list.size());
        } catch (Exception e) {
            log.error("刷新机库/无人机清单失败，保留旧清单", e);
        }
    }

    /**
     * 每 5 秒推送一次机库/无人机实时数据
     */
    @Scheduled(fixedRate = 5000)
    public void pushRealtime() {
        String token = client.getAccessToken();
        if (token == null || token.isEmpty()) {
            log.warn("统一Redis中未找到无人机平台token，跳过本轮实时推送（请确保 yz_biz_sys 已运行并获取token）");
            return;
        }
        if (dockDroneList == null || dockDroneList.isEmpty()) {
            // 首次启动缓存可能为空，主动补拉一次
            refreshDockDroneList();
            return;
        }
        for (DockDroneVO dock : dockDroneList) {
            String dockSn = dock.getDockSn();
            try {
                JSONObject dockData = client.getDockRealtime(dockSn);
                WebSocketSession.sendMessage2All(buildMessage(TYPE_DOCK, dockSn, dockData));
            } catch (Exception e) {
                log.error("推送机库实时数据失败 dock={}", dockSn, e);
            }
            // 一库多机：遍历该机库下所有绑定无人机逐一推送
            if (dock.getDroneList() != null) {
                for (DroneVO drone : dock.getDroneList()) {
                    String droneSn = drone.getDroneSn();
                    if (droneSn == null || droneSn.isEmpty()) {
                        continue;
                    }
                    try {
                        JSONObject droneData = client.getDroneRealtime(droneSn);
                        WebSocketSession.sendMessage2All(buildMessage(TYPE_DRONE, droneSn, droneData));
                    } catch (Exception e) {
                        log.error("推送无人机实时数据失败 drone={}", droneSn, e);
                    }
                }
            }
        }
    }

    private String buildMessage(String type, String sn, JSONObject data) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", type);
        msg.put("sn", sn);
        msg.put("data", data);
        return JSON.toJSONString(msg);
    }
}