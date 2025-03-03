package com.mskyeye.handler.cron;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.gson.Gson;
import com.mskyeye.common.utils.StringUtil;
import com.mskyeye.handler.model.LwCarInfoPacket;
import com.mskyeye.handler.model.YzCarInfo;
import com.mskyeye.handler.mq.MqConnectionUtil;
import com.mskyeye.handler.utils.GetRequestUtil;
import com.mskyeye.handler.utils.RedisCache;
import com.rabbitmq.client.AMQP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mskyeye.handler.common.GlobalResources.*;

/**
 * @ClassName:QueryCarInfoTask
 * @Description: 航迹服务每10s从Redis取出账号的车辆基本信息，
 * 通过http获取每个账号下的所有车载GPS信息，关联封装好后发送给消息队列（百度经纬度转成谷歌经纬度），
 * 如果有登录失败错误，需要重新登录车载平台，更新key
 * @Author:R.Gong
 * @Date:2023/10/24 14:17
 * @Version:1.0
 **/
@Component
public class QueryCarInfoTask {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private MqConnectionUtil mqConnectionUtil;

    // 设置消息的TTL为10秒
    private AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
            .expiration("10000")
            .build();

    @Scheduled(fixedRate = 10000)
    public void run() throws IOException {
        Map<String, String> map = redisCache.getCacheObject(CAR_INFO);
        List<YzCarInfo> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            list.add(JSON.parseObject(entry.getValue(), YzCarInfo.class));
        }
        //从车载GPS平台获取所有GPS信息
        if (StringUtil.isEmpty(yzGpsInfo.getUserName())
                || StringUtil.isEmpty(yzGpsInfo.getPwd())
                || StringUtil.isEmpty(yzGpsInfo.getLoginId())) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("method", "getDeviceListByCustomId");
        params.put("mds", yzGpsInfo.getMds());
        params.put("id", yzGpsInfo.getLoginId());
        params.put("mapTyp", "BAIDU");

        String result = GetRequestUtil.sendToGpsGetReq("GetDate", params);
        JSONObject resJson = JSONObject.parseObject(result);
        if (Integer.parseInt((String) resJson.get("errorCode")) == 200) {
            JSONArray resJson2 = (JSONArray) resJson.get("data");
            JSONArray carArray = ((JSONArray) (((JSONObject) resJson2.get(0)).get("records")));
            for (Object resArray : carArray) {
                if (GL_CarMap.containsKey(((JSONArray) resArray).get(12))) {
                    YzCarInfo yzCarInfo = GL_CarMap.get(((JSONArray) resArray).get(12));
//                    Double baiduLon = ((BigDecimal) ((JSONArray) resArray).get(2)).doubleValue();
//                    Double baiduLat = ((BigDecimal) ((JSONArray) resArray).get(3)).doubleValue();
                    yzCarInfo.setLon(((BigDecimal) ((JSONArray) resArray).get(2)).doubleValue());
                    yzCarInfo.setLat(((BigDecimal) ((JSONArray) resArray).get(3)).doubleValue());
                    yzCarInfo.setShowInfo(yzCarInfo.getCarPlateNumber());
                    //速度为0则停车，不为0则行驶中
                    if (Double.parseDouble((String) ((JSONArray) resArray).get(8)) > 0.0D) {
                        yzCarInfo.setCarStatus(1);
                    } else {
                        yzCarInfo.setCarStatus(2);
                    }
                    //对比信号时间（序号6）和服务器时间（序号15），相差10分钟以上则驻车
                    if ((Long) ((JSONArray) resArray).get(15) - (Long) ((JSONArray) resArray).get(6) > 600000L) {
                        yzCarInfo.setCarStatus(3);
                    }
                    switch (yzCarInfo.getCarStatus()) {
                        case 1:
                            yzCarInfo.setShowInfo(yzCarInfo.getShowInfo() + "[" + "行驶中" + "]");
                            break;
                        case 2:
                            yzCarInfo.setShowInfo(yzCarInfo.getShowInfo() + "[" + "停车" + "]");
                            break;
                        case 3:
                            yzCarInfo.setShowInfo(yzCarInfo.getShowInfo() + "[" + "驻车" + "]");
                            break;
                    }
                    LwCarInfoPacket lwCarInfoPacket = new LwCarInfoPacket();
                    lwCarInfoPacket.setCARCODE(yzCarInfo.getCarCode());
                    lwCarInfoPacket.setCARGPS(yzCarInfo.getCarGps());
                    lwCarInfoPacket.setDEPTID(yzCarInfo.getDeptId());
                    lwCarInfoPacket.setPHOTOURL(yzCarInfo.getAddrUrl());
                    lwCarInfoPacket.setLON(yzCarInfo.getLon());
                    lwCarInfoPacket.setLAT(yzCarInfo.getLat());
                    lwCarInfoPacket.setSHOWINFO(yzCarInfo.getShowInfo());
                    mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, "car_track.key",
                            properties, new Gson().toJson(lwCarInfoPacket).getBytes(StandardCharsets.UTF_8));
                }
            }

        }
        //需要重新登录
        else if (Integer.parseInt((String) resJson.get("errorCode")) == 403) {
            Map<String, String> params1 = new HashMap<>();
            params1.put("LoginName", yzGpsInfo.getUserName());
            params1.put("LoginPassword", yzGpsInfo.getPwd());
            params1.put("LoginType", "ENTERPRISE");
            params1.put("language", "cn");
            params1.put("ISMD5", "0");
            params1.put("timeZone", "+08");
            params1.put("apply", "APP");

            String result1 = GetRequestUtil.sendToGpsGetReq("loginSystem", params);
            JSONObject resJson1 = JSONObject.parseObject(result1);
            if (Integer.parseInt((String) resJson1.get("errorCode")) == 200) {
                yzGpsInfo.setUserName(yzGpsInfo.getUserName());
                yzGpsInfo.setPwd(yzGpsInfo.getPwd());
                yzGpsInfo.setLoginId((String) resJson.get("id"));
                yzGpsInfo.setMds((String) resJson.get("mds"));
            }
        }
    }

    // 百度坐标系 (BD-09) 到 火星坐标系 (GCJ-02) 的转换算法
    private double[] bd09togcj02(double bd_lon, double bd_lat) {
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * Math.PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * Math.PI);
        double gg_lng = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new double[]{gg_lng, gg_lat};
    }

}
