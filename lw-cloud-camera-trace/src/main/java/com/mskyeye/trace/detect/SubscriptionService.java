package com.mskyeye.trace.detect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import static com.mskyeye.trace.common.GlResources.MSG_TOPIC;

/**
 * @ClassName:SubscriptionService
 * @Description:监听器注册
 * @Author:R.Gong
 * @Date:2023/8/30 14:57
 * @Version:1.0
 **/
@Service
public class SubscriptionService {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @Autowired
    private MessageReceiver messageReceiver;

    @Bean
    public void subscribe() {
        RedisConnection redisConnection = redisConnectionFactory.getConnection();
        redisConnection.subscribe(messageReceiver, new ChannelTopic(MSG_TOPIC).getTopic().getBytes());
    }
}
