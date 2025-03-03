package com.mskyeye.handler.cron;


import com.mskyeye.common.utils.RedisCacheKey;
import com.mskyeye.handler.common.GlobalResources;
import com.mskyeye.handler.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @ClassName:TraversalListTask
 * @Description:定时获取redis中的AIS静态数据(60s周期)
 * @Author:R.Gong
 * @Date:2022/11/26 14:30
 * @Version:1.0
 **/
@Component
public class QueryAIsStaticDataTask {

    @Autowired
    private RedisCache redisCache;

    @Scheduled(fixedRate = 60000)
    public void run() {
       GlobalResources.aisStaticInfoMap = redisCache.getCacheObject(RedisCacheKey.AIS_STATIC_INFO);
    }
}
