package com.mskyeye.iot.mq.handler;

import com.google.gson.Gson;
import com.mskyeye.iot.mq.util.MqConnectionUtil;
import com.mskyeye.lwradarstationdata.protocol.ais.YzAisStaticInfo;
import com.rabbitmq.client.AMQP;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @ClassName:AisStaticDataToMqHandler
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/4/26 13:57
 * @Version:1.0
 **/
@Component
@Slf4j
@ChannelHandler.Sharable
public class AisStaticDataToMqHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    private MqConnectionUtil mcUtil;

    // 设置消息的TTL为10秒
    private AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
            .expiration("10000")
            .build();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        YzAisStaticInfo rtp = (YzAisStaticInfo) msg;
        mcUtil.getChannel().basicPublish(mcUtil.EXCHANGE_NAME,"ais.static.data.key",
                properties, new Gson().toJson(rtp).getBytes(StandardCharsets.UTF_8));
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
