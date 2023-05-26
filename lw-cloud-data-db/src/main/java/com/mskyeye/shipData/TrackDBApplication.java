package com.mskyeye.shipData;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

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
@MapperScan({"com.mskyeye.shipData.mapper"})
public class TrackDBApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackDBApplication.class, args);
    }

}
