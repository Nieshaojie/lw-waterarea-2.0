package com.mskyeye.iot.mq.handler;

import com.google.gson.Gson;
import com.mskyeye.iot.mq.util.MqConnectionUtil;
import com.mskyeye.iot.utils.ProConvClazz;
import com.mskyeye.lwradarstationdata.protocol.radar.custom.LwTrackPacket;
import com.mskyeye.lwradarstationdata.protocol.radar.custom.TrackTcpPacket;
import com.rabbitmq.client.AMQP;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @ClassName:TrackToMqHandler
 * @Description:将航迹数据解析并发送到MQ
 * @Author:R.Gong
 * @Date:2022/11/17 13:48
 * @Version:1.0
 **/
@Component
@Slf4j
@ChannelHandler.Sharable
public class TrackToMqHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    private MqConnectionUtil mcUtil;

    // 设置消息的TTL为1秒
    private AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
            .expiration("1000")
            .build();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof TrackTcpPacket) {
            TrackTcpPacket rtp = (TrackTcpPacket) msg;
            LwTrackPacket trackWSPacket = ProConvClazz.trackTcp2WS(rtp);
            mcUtil.getChannel().basicPublish(mcUtil.EXCHANGE_NAME,"track.key",
                    properties, new Gson().toJson(trackWSPacket).getBytes(StandardCharsets.UTF_8));
        }
        else {
            //由下一个handler处理
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
