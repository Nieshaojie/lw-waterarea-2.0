package com.mskyeye.ws.utils;

import com.mskyeye.lwradarstationdata.protocol.track.Content;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 历史船舶信息发送器（外轮/国轮统一入库）
 */
public class ShipInfoSender {

    private static final String SHIP_INFO_URL = "http://192.168.0.171:8081/system/foreignShip/info";

    /**
     * 发送船舶信息（历史船舶信息入库）
     * @param packet Content 航迹数据对象
     * @param shipCategory 船籍：0=外轮，1=国轮
     * @return 接口响应内容
     */
    public static String sendShipInfo(Content packet, int shipCategory) {
        RestTemplate restTemplate = new RestTemplate();

        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("mmsi", packet.getMMSI());
        requestBody.put("time", System.currentTimeMillis());
        requestBody.put("lat", packet.getLAT());
        requestBody.put("lon", packet.getLON());
        requestBody.put("shipName", packet.getSHIPNAME());
        requestBody.put("imo", packet.getIMO());
        requestBody.put("country", packet.getCOUNTRY());
        requestBody.put("shipCategory", shipCategory);

        // 构造请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 发送 POST 请求
        ResponseEntity<String> response = restTemplate.postForEntity(SHIP_INFO_URL, entity, String.class);

        return response.getBody();
    }
}
