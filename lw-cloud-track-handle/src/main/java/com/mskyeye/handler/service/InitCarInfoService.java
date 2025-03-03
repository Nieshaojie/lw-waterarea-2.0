package com.mskyeye.handler.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mskyeye.common.utils.StringUtil;
import com.mskyeye.handler.mapper.YzCarInfoMapper;
import com.mskyeye.handler.model.YzCarInfo;
import com.mskyeye.handler.utils.GetRequestUtil;
import com.mskyeye.handler.utils.RedisCache;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mskyeye.handler.common.GlobalResources.*;

/**
 * @ClassName:InitCarInfoService
 * @Description:初始化车辆信息服务
 * @Author:R.Gong
 * @Date:2023/10/24 9:05
 * @Version:1.0
 **/
@Component
@RefreshScope
@Slf4j
@Data
public class InitCarInfoService {

    @Autowired
    private YzCarInfoMapper yzCarInfoMapper;

    @Autowired
    private RedisCache redisCache;

    @Value("${gps_username}")
    private String gpsUserName;

    @Value("${gps_password}")
    private String gpsPwd;

    @Value("${is_enable_gps}")
    private String isEnableGps;

    public void initCarInfo() throws Exception{
        //初始化车辆信息到Redis
        List<YzCarInfo> list =  yzCarInfoMapper.selectYzCarInfoList(null);

        Map<String,String> map = list.stream().collect(Collectors.toMap(e->e.getCarGps(), e-> JSON.toJSONString(e)));

        GL_CarMap.putAll(list.stream().collect(Collectors.toMap(e->e.getCarGps(), e-> e)));

        redisCache.setCacheObject(CAR_INFO,map);

        //登录车载GPS平台
        if(isEnableGps.equals("0") || StringUtil.isEmpty(gpsUserName) || StringUtil.isEmpty(gpsPwd)){
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("LoginName",gpsUserName);
        params.put("LoginPassword",gpsPwd);
        params.put("LoginType","ENTERPRISE");
        params.put("language","cn");
        params.put("ISMD5","0");
        params.put("timeZone","+08");
        params.put("apply","APP");

        String result = GetRequestUtil.sendToGpsGetReq("loginSystem",params);
        JSONObject resJson = JSONObject.parseObject(result);
        if((int)resJson.get("errorCode") == 200){
            yzGpsInfo.setUserName(gpsUserName);
            yzGpsInfo.setPwd(gpsPwd);
            yzGpsInfo.setLoginId((String) resJson.get("id"));
            yzGpsInfo.setMds((String) resJson.get("mds"));
        }
    }
}
