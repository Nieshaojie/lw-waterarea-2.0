package com.mskyeye.alarm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @ClassName:AlarmDataApplication
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2022/10/19 14:27
 * @Version:1.0
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class AlarmDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlarmDataApplication.class, args);
    }

}
