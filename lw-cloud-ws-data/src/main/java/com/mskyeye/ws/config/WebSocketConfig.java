package com.mskyeye.ws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yeauty.standard.ServerEndpointExporter;

/**
 * @ClassName:WebSocketConfig
 * @Description:websocket配置类
 * @Author:R.Gong
 * @Date:2022/7/20 10:53
 * @Version:1.0
 **/

@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }
}
