package com.mskyeye.ws.mq.utils;

import com.alibaba.fastjson2.JSONObject;
import com.mskyeye.common.utils.StringUtil;
import com.mskyeye.lwradarstationdata.protocol.track.Content;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;
import com.mskyeye.ws.redis.utils.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 平台消息发送器
 * 负责将内部航迹数据结构（LwTrackPacket）转换为平台标准 JSON 并通过 MQTT 发布
 */
@Slf4j
@Service
public class MqttMessageSender {

    @Resource
    private MqttClientManager mqttClientManager;
    @Autowired
    RedisCache redisCache;

    // 配置文件参数
    @Value("${mqtt.topic}")
    private String topic;

    @Value("${mqtt.qos}")
    private int qos;

    @Value("${mqtt.sourceVendor}")
    private String sourceVendor;

    @Value("${mqtt.testType}")
    private String testType;

    // Redis缓存相关配置
    private static final String TRACK_CACHE_PREFIX = "track_time:";
    private static final String ALARM_CACHE_PREFIX = "alarm_time:";
    private static final Integer CACHE_EXPIRE_SECONDS = 10;

    /**
     * 生成目标唯一键
     */
    private String generateTargetKey(Content content) {
        return TRACK_CACHE_PREFIX + content.getTID() + "_" +
                (content.getSN() != null ? content.getSN() : "unknown");
    }

    private String generateAlarmKey(Content content) {
        return ALARM_CACHE_PREFIX + content.getTID() + "_" +
                (content.getSN() != null ? content.getSN() : "unknown");
    }

    /**
     * 获取目标的首次发现时间
     * 如果目标不存在，则记录当前时间为首次发现时间
     */
    public Long getFirstDiscoveryTime(Content content) {
        String targetKey = generateTargetKey(content);
        Long firstSendTime = redisCache.getCacheObject(targetKey);
        long currentTime = System.currentTimeMillis();

        if (firstSendTime == null) {
            // 目标首次出现，记录当前时间为首次发现时间
            redisCache.setCacheObject(targetKey, currentTime, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            return currentTime;
        } else {
            // 目标已存在，更新缓存过期时间，返回首次发现时间
            redisCache.expire(targetKey, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            return firstSendTime;
        }
    }

    public Long getFirstAlarmTime(Content content) {
        String targetKey = generateAlarmKey(content);
        Long firstSendTime = redisCache.getCacheObject(targetKey);
        long currentTime = System.currentTimeMillis();

        if (firstSendTime == null) {
            // 目标首次出现，记录当前时间为首次发现时间
            redisCache.setCacheObject(targetKey, currentTime, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            return currentTime;
        } else {
            // 目标已存在，更新缓存过期时间，返回首次发现时间
            redisCache.expire(targetKey, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            return firstSendTime;
        }
    }

    /**
     * 将航迹数据转换为平台标准格式并发布
     */
    public void sendTrackToPlatform(LwTrackPacket packet) {
        try {
            if (packet == null || packet.getITEM() == null || packet.getITEM().isEmpty()) {
                log.warn("航迹包为空，跳过发送");
                return;
            }

            Content c = packet.getITEM().get(0); // 取第一个航迹目标

            // 获取目标的首次发现时间
            Long firstDiscoveryTime = getFirstDiscoveryTime(c);
            long currentTime = System.currentTimeMillis();

            JSONObject json = new JSONObject();

            // === 按照平台文档字段映射 ===
            json.put("lng", c.getLON());                         // 经度
            json.put("lat", c.getLAT());                         // 纬度
            json.put("alt", c.getALT() != null ? c.getALT() : 0);// 高度（米）
            json.put("velocityH", c.getSPEED());                 // 水平速度 (m/s)
            json.put("headH", c.getCOURSE());                    // 航向角 (度)
            json.put("sourceVendor", sourceVendor);              // 厂商 ID
            json.put("testType", testType);                      // 测试类型
            json.put("deviceNo", String.valueOf(c.getSTATIONID()));// 探测设备编号

            // 构造批次编号（厂商id-设备号-时间戳）
            json.put("batchNumAndSerial", sourceVendor + "-" + c.getSTATIONID() + "-" + firstDiscoveryTime);
            json.put("uavSn", c.getSN() != null ? new String(c.getSN().getBytes(), StandardCharsets.UTF_8) : "unknown"); // 无人机 SN
            json.put("batchNum", c.getSN() != null ? new String(c.getSN().getBytes(), StandardCharsets.UTF_8) : "unknown"); // 与 uavSn 一致
            json.put("time", currentTime);        // 当前 UTC 毫秒时间戳
            json.put("T2", currentTime);            //稳定跟踪时间
            json.put("T1", firstDiscoveryTime);//系统发现无人机的时间
            if(StringUtil.isNotEmpty(packet.getITEM().get(0).getALARM())){
                Long firstAlarmTime = getFirstAlarmTime(c);
                json.put("T3", firstAlarmTime);
            }
            json.put("sourceType", "fusion");                     // 数据来源类型
            json.put("velocityV", 0.0);                          // 垂直速度（默认 0）
            json.put("type", "1");                               // 固定值：监视类目标数据

            log.info("当前融合信息发送mqtt消息为：{}",json.toJSONString());
            // === MQTT 发布 ===
            mqttClientManager.publish(topic, json.toJSONString(), qos);
        } catch (Exception e) {
            log.error("发送航迹数据到平台失败", e);
        }
    }

    /**
     * 将航迹告警数据转换为平台标准格式并发布
     */
    public void sendTrackToPlatformRD(LwTrackPacket packet) {
        try {
            if (packet == null || packet.getITEM() == null || packet.getITEM().isEmpty()) {
                log.warn("航迹包为空，跳过发送");
                return;
            }

            Content c = packet.getITEM().get(0); // 取第一个航迹目标
            // 获取目标的首次发现时间
            Long firstDiscoveryTime = getFirstDiscoveryTime(c);
            long currentTime = System.currentTimeMillis();
            JSONObject json = new JSONObject();

            // === 按照平台文档字段映射 ===
            json.put("lng", c.getLON());                         // 经度
            json.put("lat", c.getLAT());                         // 纬度
            json.put("alt", c.getALT() != null ? c.getALT() : 0);// 高度（米）
            json.put("velocityH", c.getSPEED());                 // 水平速度 (m/s)
            json.put("headH", c.getCOURSE());                    // 航向角 (度)
            json.put("sourceVendor", sourceVendor);              // 厂商 ID
            json.put("testType", testType);                      // 测试类型
            json.put("deviceNo", String.valueOf(c.getSTATIONID()));// 探测设备编号

            // 构造批次编号（厂商id-设备号-时间戳）
            json.put("batchNumAndSerial", sourceVendor + "-" + c.getSTATIONID() + "-" + firstDiscoveryTime);
            json.put("uavSn", c.getSN() != null ? c.getSN() : "unknown"); // 无人机 SN
            json.put("batchNum", c.getSN() != null ? c.getSN() : "unknown"); // 与 uavSn 一致
            json.put("time", currentTime);        // 当前 UTC 毫秒时间戳
            json.put("T1", firstDiscoveryTime);//系统发现无人机的时间
            json.put("T2", currentTime);
            json.put("T3", getFirstAlarmTime(c));//第一次告警时间
            json.put("sourceType", "radar");                     // 数据来源类型
            json.put("velocityV", 0.0);                          // 垂直速度（默认 0）
            json.put("type", "1");                               // 固定值：监视类目标数据

            log.info("--------当前雷达告警信息发送mqtt消息为--------：{}",json.toJSONString());
            // === MQTT 发布 ===
            mqttClientManager.publish(topic, json.toJSONString(), qos);
        } catch (Exception e) {
            log.error("发送航迹数据到平台失败", e);
        }
    }
}

