package com.mskyeye.handler.cron;


import com.mskyeye.handler.common.GlobalResources;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * @ClassName:TraversalListTask
 * @Description:定时遍历雷达和AIS缓存链表任务
 * @Author:R.Gong
 * @Date:2022/11/26 14:30
 * @Version:1.0
 **/
@Component
public class TraversalListTask {

    @Scheduled(fixedRate = 30000)
    public void run() {
        GlobalResources.radarTrackList.stream().filter(obj -> {
            return System.currentTimeMillis() - obj.getRefreshTime() < 180000;
        }).collect(Collectors.toList());

        GlobalResources.aisTrackList.stream().filter(obj -> {
            return System.currentTimeMillis() - obj.getRefreshTime() < 180000;
        }).collect(Collectors.toList());
    }
}
