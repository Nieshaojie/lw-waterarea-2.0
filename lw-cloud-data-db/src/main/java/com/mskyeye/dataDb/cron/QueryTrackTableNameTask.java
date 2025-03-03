package com.mskyeye.dataDb.cron;

import com.mskyeye.common.utils.StringUtil;
import com.mskyeye.dataDb.config.InfluxDBConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.mskyeye.dataDb.common.GlobalResources.bQueryTrackTable;
import static com.mskyeye.dataDb.common.GlobalResources.trackTableNameList;

/**
 * @ClassName:QueryTrackTableNameTask
 * @Description:定时检索航迹表
 * @Author:R.Gong
 * @Date:2023/8/17 21:41
 * @Version:1.0
 **/
@Component
public class QueryTrackTableNameTask {

    @Autowired
    private InfluxDBConfig influxDBConfig;

    @Scheduled(fixedRate = 5000)
    public void run() {
        trackTableNameList.clear();
        if(StringUtil.isNotEmpty(influxDBConfig.influxDB)){
            trackTableNameList.addAll(influxDBConfig.queryTableNames());
            bQueryTrackTable = true;
        }
    }
}
