package com.mskyeye.dataDb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * @ClassName:TrackDBApplication
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2022/12/21 14:27
 * @Version:1.0
 **/
@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
@MapperScan({"com.mskyeye.dataDb.mapper"})
public class TrackDBApplication {
    //将时间改为第8时区的时间
    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
    }
    public static void main(String[] args) {
        SpringApplication.run(TrackDBApplication.class, args);
    }

}
