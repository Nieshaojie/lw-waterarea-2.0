package com.mskyeye.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * @ClassName:IotCmsApplication
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2022/10/19 17:01
 * @Version:1.0
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class IotCmsApplication {

    //将时间改为第8时区的时间
    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
    }
    public static void main(String[] args) {
        SpringApplication.run(IotCmsApplication.class, args);
    }
}
