package com.mskyeye.merge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

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
public class TrackMergeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackMergeApplication.class, args);
    }

}
