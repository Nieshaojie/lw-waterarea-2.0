package com.mskyeye.ws.utils;

import com.mskyeye.lwradarstationdata.protocol.track.Content;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

public class AlarmInfoSender {

    private static final String ALARM_INFO_URL = "http://localhost:8081/system/alarm/info";

    /**
     * 发送预警信息
     * @param packet LwTrackPacket 数据对象
     * @return 接口响应内容
     */
    public static String sendAlarmInfo(Content packet) {
        RestTemplate restTemplate = new RestTemplate();

        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", packet.getTID());
        requestBody.put("alarm", packet.getALARM());
        requestBody.put("lat", packet.getLAT());
        requestBody.put("lon", packet.getLON());

        // 构造请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 发送 POST 请求
        ResponseEntity<String> response = restTemplate.postForEntity(ALARM_INFO_URL, entity, String.class);

        return response.getBody();
    }
}
