package com.mskyeye.dataDb.Handler;

import com.alibaba.fastjson.JSON;
import com.mskyeye.common.utils.RedisCacheKey;
import com.mskyeye.dataDb.common.GlobalResources;
import com.mskyeye.dataDb.service.IYzAisStaticInfoService;
import com.mskyeye.dataDb.utils.MqConnectionUtil;
import com.mskyeye.dataDb.utils.RedisCache;
import com.mskyeye.lwradarstationdata.protocol.ais.YzAisStaticInfo;
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
public class AisHandlerService {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private MqConnectionUtil mqConnectionUtil;

    @Autowired
    private IYzAisStaticInfoService iYzAisStaticInfoService;

    public void run() throws Exception {
        //初始化消息队列配置
//        mqConnectionUtil.initMqConfig();

        // 定义队列的消费者
        DefaultConsumer consumer = new DefaultConsumer(mqConnectionUtil.getChannel()) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @SneakyThrows
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                //解析成AIS静态数据对象
                YzAisStaticInfo newInfo = JSON.parseObject(new String(body, CharsetUtil.UTF_8), YzAisStaticInfo.class);
                //如果缓存中没有该静态数据,进行数据新增
                if (!GlobalResources.aisStaticDataMap.containsKey(newInfo.getMmsi())) {
                    GlobalResources.aisStaticDataMap.put(newInfo.getMmsi(), newInfo);
                    //redis更新
//                    Map<Long,YzAisStaticInfo> map = GlobalResources.aisStaticDataMap;
                    redisCache.setCacheObject(RedisCacheKey.AIS_STATIC_INFO, GlobalResources.aisStaticDataMap);
                    //数据库新增
                    iYzAisStaticInfoService.insertYzAisStaticInfo(newInfo);
                } else {
                    YzAisStaticInfo oldInfo = GlobalResources.aisStaticDataMap.get(newInfo.getMmsi());
                    //如果信息一致,不用更新
                    if (oldInfo.equals(newInfo)) {
                        return;
                    }
                    boolean bUpdataData = false;
                    //更新imo
                    if (oldInfo.getImo() == null && newInfo.getImo() != null) {
                        oldInfo.setImo(newInfo.getImo());
                        bUpdataData = true;
                    }
                    //更新吃水深度
                    if (oldInfo.getDraught() == null && newInfo.getDraught() != null) {
                        oldInfo.setDraught(newInfo.getDraught());
                        bUpdataData = true;
                    }
                    //更新呼号
                    if (oldInfo.getCallSign() == null && newInfo.getCallSign() != null) {
                        oldInfo.setCallSign(newInfo.getCallSign());
                        bUpdataData = true;
                    }
                    //更新船舶类型
                    if (oldInfo.getShipType() == null && newInfo.getShipType() != null) {
                        oldInfo.setShipType(newInfo.getShipType());
                        bUpdataData = true;
                    }
                    //更新船名
                    if (oldInfo.getShipName() == null && newInfo.getShipName() != null) {
                        oldInfo.setShipName(newInfo.getShipName());
                        bUpdataData = true;
                    }
                    if(bUpdataData){
                        //redis更新
                        redisCache.setCacheObject(RedisCacheKey.AIS_STATIC_INFO, GlobalResources.aisStaticDataMap);
                        //数据库新增
                        iYzAisStaticInfoService.updateYzAisStaticInfo(oldInfo);
                    }
                }
            }
        };
        // 监听队列
        mqConnectionUtil.getChannel().basicConsume(mqConnectionUtil.AIS_STATIC_QUEUE_NAME, true, consumer);
    }
}
