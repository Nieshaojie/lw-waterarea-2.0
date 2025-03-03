package com.mskyeye.ws.cron;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mskyeye.ws.model.DeviceInDept;
import com.mskyeye.ws.redis.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.mskyeye.ws.common.GlobalResources.deviceInfoMap;

/**
 * @ClassName:QueryCameraStatusTask
 * @Description:10s更新一次设备信息
 * @Author:R.Gong
 * @Date:2023/8/7 18:58
 * @Version:1.0
 **/
@Component
public class RefreshDeviceInfoTask {

    private static final String DEPT_DEVICE_KEY = "yz2.0_info:dept_device_tokens:";

    @Autowired
    private RedisCache redisCache;
    /**
     * 10s更新一次设备信息
     */
    @Scheduled(fixedRate = 10000)
    public void QueryDeviceInfo() throws Exception {
        try {
            List<JSONObject> cacheList = redisCache.getCacheList(DEPT_DEVICE_KEY);
            ObjectMapper mapper = new ObjectMapper();
            List<DeviceInDept> list = cacheList.stream()
                    .map(jsonObject -> mapper.convertValue(jsonObject, DeviceInDept.class))
                    .collect(Collectors.toList());
            deviceInfoMap.clear();
            list.forEach(obj->{
                deviceInfoMap.put(obj.getDeptId(),obj);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
