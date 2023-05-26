package com.mskyeye.shipData.Handler;

import com.alibaba.fastjson.JSON;
import com.mskyeye.lwradarstationdata.protocol.ais.YzAisStaticInfo;
import com.mskyeye.shipData.common.GlobalResources;
import com.mskyeye.shipData.service.IYzAisStaticInfoService;
import com.mskyeye.shipData.utils.AisMqConnUtil;
import com.mskyeye.shipData.utils.RedisCache;
import com.mskyeye.shipData.utils.TrackMqConnUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @ClassName:AisHandlerService
 * @Description:AIS静态数据处理
 * @Author:R.Gong
 * @Date:2023/5/25 11:10
 * @Version:1.0
 **/
@Component
@RefreshScope
@Slf4j
public class AisHandlerService{

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private AisMqConnUtil aisMqConnUtil;

    @Autowired
    private IYzAisStaticInfoService iYzAisStaticInfoService;

    public void run() throws Exception {
        //初始化消息队列配置
        aisMqConnUtil.initMqConfig();

        // 定义队列的消费者
        DefaultConsumer consumer = new DefaultConsumer(aisMqConnUtil.getChannel()) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @SneakyThrows
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                //解析成AIS静态数据对象
                YzAisStaticInfo yzAisStaticInfo = JSON.parseObject(new String(body, CharsetUtil.UTF_8), YzAisStaticInfo.class);
                //如果缓存中没有该静态数据,进行数据更新
                if(!GlobalResources.aisStaticDataMap.containsKey(yzAisStaticInfo.getMmsi())){
                    GlobalResources.aisStaticDataMap.put(yzAisStaticInfo.getMmsi(),yzAisStaticInfo);
                    //redis更新
                    redisCache.setCacheConMap("AIS_STATIC_INFO",GlobalResources.aisStaticDataMap);
                    //数据库更新
                    iYzAisStaticInfoService.insertYzAisStaticInfo(yzAisStaticInfo);
                }
            }
        };
        // 监听队列
        aisMqConnUtil.getChannel().basicConsume(TrackMqConnUtil.QUEUE_NAME, true, consumer);
    }
}
