package com.mskyeye.ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @ClassName:WsDataApplication
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2022/10/19 14:27
 * @Version:1.0
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class WsDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(WsDataApplication.class, args);
    }

}
