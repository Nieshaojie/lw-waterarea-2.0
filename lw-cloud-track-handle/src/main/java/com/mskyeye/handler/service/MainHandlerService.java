package com.mskyeye.handler.service;

import com.mskyeye.handler.mq.MqConnectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @ClassName:MainHandlerService
 * @Description:主处理器服务
 * @Author:R.Gong
 * @Date:2023/5/31 15:44
 * @Version:1.0
 **/
@Component
@Slf4j
public class MainHandlerService implements ApplicationRunner {

    @Autowired
    private MergeAndAlarmHandleService mergeAndAlarmHandleService;

    @Autowired
    private InitCarInfoService initCarInfoService;

    @Autowired
    private MqConnectionUtil mqConnUtil;

    public void run(ApplicationArguments args) throws Exception {

        //初始化消息队列配置
        mqConnUtil.initMqConfig();

        mergeAndAlarmHandleService.run();

        initCarInfoService.initCarInfo();
    }
}
