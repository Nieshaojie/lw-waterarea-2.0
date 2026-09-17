package com.mskyeye.ws.platform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mskyeye.ws.redis.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 第三方无人机平台(cmii)实时数据客户端。
 * token 从统一 Redis 读取（由 yz_biz_sys 权威维护），请求携带
 * tenant-id / x-uas-type / Bearer，返回实时数据。同时支持动态查询机库及绑定无人机清单。
 *
 * @author mskyeye
 */
@Component
public class CmiiRealtimeClient {

    private static final int TIMEOUT_MS = 8000;

    @Autowired
    private CmiiProperties props;

    @Autowired
    private RedisCache redisCache;

    /** 从统一 Redis 读取无人机平台 token */
    public String getAccessToken() {
        return redisCache.getCacheObject(props.getTokenRedisKey());
    }

    /** 无人机实时数据查询 */
    public JSONObject getDroneRealtime(String droneSn) {
        return request(props.getDataUrl() + "/admin-api/uas/surveillance/realtime/drone/" + droneSn);
    }

    /** 机库实时数据查询 */
    public JSONObject getDockRealtime(String dockSn) {
        return request(props.getDataUrl() + "/admin-api/uas/surveillance/realtime/dock/" + dockSn);
    }

    /**
     * 按租户查询所有机库及其绑定无人机（data 为数组）
     */
    public List<DockDroneVO> getDockDroneList() {
        String url = props.getDataUrl() + "/admin-api/api/device/drone/get_dock_drone_sn";
        String resp = httpsGet(url);
        JSONObject json = JSON.parseObject(resp);
        Integer code = json.getInteger("code");
        if (code == null || code != 0) {
            throw new IllegalStateException("查询机库及绑定无人机清单失败 code=" + code + ", msg=" + json.getString("msg"));
        }
        JSONArray arr = json.getJSONArray("data");
        if (arr == null || arr.isEmpty()) {
            return Collections.emptyList();
        }
        List<DockDroneVO> result = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            JSONObject dock = arr.getJSONObject(i);
            DockDroneVO vo = new DockDroneVO();
            vo.setDockId(dock.getLong("dockId"));
            vo.setDockSn(dock.getString("dockSn"));
            vo.setDockName(dock.getString("dockName"));
            JSONArray droneArr = dock.getJSONArray("droneList");
            List<DroneVO> droneList = new ArrayList<>();
            if (droneArr != null && !droneArr.isEmpty()) {
                for (int j = 0; j < droneArr.size(); j++) {
                    JSONObject d = droneArr.getJSONObject(j);
                    DroneVO dv = new DroneVO();
                    dv.setDroneId(d.getLong("droneId"));
                    dv.setDroneSn(d.getString("droneSn"));
                    dv.setDroneName(d.getString("droneName"));
                    droneList.add(dv);
                }
            }
            vo.setDroneList(droneList);
            result.add(vo);
        }
        return result;
    }

    /**
     * 通用查询：data 为 Object（实时数据）
     */
    private JSONObject request(String url) {
        String resp = httpsGet(url);
        JSONObject json = JSON.parseObject(resp);
        Integer code = json.getInteger("code");
        if (code == null || code != 0) {
            throw new IllegalStateException("查询实时数据失败 code=" + code + ", msg=" + json.getString("msg"));
        }
        return json.getJSONObject("data");
    }

    private String httpsGet(String targetUrl) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("tenant-id", props.getTenantId());
        if (props.getUasType() != null && !props.getUasType().isEmpty()) {
            headers.put("x-uas-type", props.getUasType());
        }
        String token = getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("统一Redis中未找到无人机平台token，请先启动 yz_biz_sys");
        }
        headers.put("Authorization", "Bearer " + token);

        BufferedReader in = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[] { new X509TrustManager() {
                @Override public void checkClientTrusted(X509Certificate[] chain, String authType) { }
                @Override public void checkServerTrusted(X509Certificate[] chain, String authType) { }
                @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            } }, new SecureRandom());

            URL url = new URL(targetUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier((hostname, session) -> true);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "application/json");
            headers.forEach(conn::setRequestProperty);
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("调用无人机平台实时接口失败: " + e.getMessage(), e);
        } finally {
            if (in != null) {
                try { in.close(); } catch (Exception ignored) { }
            }
        }
    }
}