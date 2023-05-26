package com.mskyeye.shipData.Handler;

import com.mskyeye.lwradarstationdata.protocol.ais.YzAisStaticInfo;
import com.mskyeye.shipData.common.GlobalResources;
import com.mskyeye.shipData.service.IYzAisStaticInfoService;
import com.mskyeye.shipData.utils.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName:MainHandlerService
 * @Description:主处理器服务
 * @Author:R.Gong
 * @Date:2023/5/25 10:56
 * @Version:1.0
 **/
@Component
@Slf4j
public class MainHandlerService implements ApplicationRunner {

    @Autowired
    private IYzAisStaticInfoService iYzAisStaticInfoService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private TrackHandlerService trackHandlerService;

    @Autowired
    private AisHandlerService aisHandlerService;

    public void run(ApplicationArguments args) throws Exception {

        //从数据库查询AIS静态信息
        List<YzAisStaticInfo> list = iYzAisStaticInfoService.selectYzAisStaticInfoList(null);
        //拉到redis
        if(list != null){
            list.forEach(obj->{
                GlobalResources.aisStaticDataMap.put(obj.getMmsi(),obj);
            });
            redisCache.setCacheConMap("AIS_STATIC_INFO",GlobalResources.aisStaticDataMap);
        }

        //航迹数据处理
        trackHandlerService.run();

        //AIS静态数据处理
        aisHandlerService.run();


//        new Thread(()->{
//            while (true){
//
//            }
//        }).start();
    }
}
