package com.mskyeye.handler;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * @ClassName:TrackMergeApplication
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2022/12/21 14:27
 * @Version:1.0
 **/
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@MapperScan({"com.mskyeye.handler.mapper"})
public class TrackHandleApplication {

    //将时间改为第8时区的时间
    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
    }
    public static void main(String[] args) {
        SpringApplication.run(TrackHandleApplication.class, args);
    }

}
