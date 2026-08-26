package com.mskyeye.ws.utils;

import com.mskyeye.lwradarstationdata.protocol.track.Content;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

public class ForeignShipInfoSender {

    private static final String FOREIGN_SHIP_INFO_URL = "http://192.168.0.171:8081/system/foreignShip/info";

    /**
     * 发送外轮船舶信息（历史外轮信息入库）
     * @param packet Content 航迹数据对象
     * @return 接口响应内容
     */
    public static String sendForeignShipInfo(Content packet) {
        RestTemplate restTemplate = new RestTemplate();

        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("mmsi", packet.getMMSI());
        requestBody.put("shipName", packet.getSHIPNAME());
        requestBody.put("imo", packet.getIMO());
        requestBody.put("country", packet.getCOUNTRY());
        requestBody.put("lat", packet.getLAT());
        requestBody.put("lon", packet.getLON());
        requestBody.put("time", System.currentTimeMillis());

        // 构造请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 发送 POST 请求
        ResponseEntity<String> response = restTemplate.postForEntity(FOREIGN_SHIP_INFO_URL, entity, String.class);

        return response.getBody();
    }
}
