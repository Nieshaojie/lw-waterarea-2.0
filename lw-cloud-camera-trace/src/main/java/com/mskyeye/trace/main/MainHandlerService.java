package com.mskyeye.trace.main;

import com.alibaba.fastjson.JSON;
import com.mskyeye.trace.model.YzAiCruiseInfo;
import com.mskyeye.trace.mq.handler.TrackListenerService;
import com.mskyeye.trace.mq.utils.MqConnectionUtil;
import com.mskyeye.trace.service.IYzAiCruiseInfoService;
import com.mskyeye.trace.utils.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mskyeye.trace.common.GlResources.CRUISE_STATE;
import static com.mskyeye.trace.common.GlResources.GL_CruiseMap;

/**
 * @ClassName:MainHandlerService
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/8/9 13:01
 * @Version:1.0
 **/
@Component
@Slf4j
public class MainHandlerService  implements ApplicationRunner {

    @Autowired
    private MqConnectionUtil mqConnUtil;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private TrackListenerService trackListenerService;

    @Autowired
    private IYzAiCruiseInfoService iYzAiCruiseInfoService;


    public void run(ApplicationArguments args) throws Exception {

        //初始化消息队列配置
        mqConnUtil.initMqConfig();

        //初始化AI巡航状态到Redis
        List<YzAiCruiseInfo> list =  iYzAiCruiseInfoService.selectYzAiCruiseInfoList(null);

        Map<Long,String> map = list.stream().collect(Collectors.toMap(e->e.getId(), e-> JSON.toJSONString(e)));

        GL_CruiseMap.putAll(list.stream().collect(Collectors.toMap(e->e.getId(), e-> e)));

        redisCache.setCacheObject(CRUISE_STATE,map);

        //运行航迹接收器
        trackListenerService.run();

    }
}
