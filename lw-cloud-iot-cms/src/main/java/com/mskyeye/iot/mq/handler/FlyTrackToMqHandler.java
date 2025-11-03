package com.mskyeye.iot.mq.handler;

import com.google.gson.Gson;
import com.mskyeye.iot.mq.util.MqConnectionUtil;
import com.mskyeye.iot.utils.ProConvClazz;
import com.mskyeye.lwradarstationdata.protocol.radar.custom.FlyTrackTcpPacket;
import com.mskyeye.lwradarstationdata.protocol.radar.custom.TrackTcpPacket;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;
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
public class FlyTrackToMqHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    private MqConnectionUtil mcUtil;

    // 设置消息的TTL为10秒
    private AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
            .expiration("10000")
            .build();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FlyTrackTcpPacket) {
            FlyTrackTcpPacket rtp = (FlyTrackTcpPacket) msg;
//            log.info("收到反无航迹信息：{}",rtp);
//            rtp.setISpeed(rtp.getISpeed()*1.944f);//转换成节
            LwTrackPacket trackWSPacket = ProConvClazz.flyTrackTcp2WS(rtp);
            log.info("对象转换：{}",trackWSPacket.getITEM().get(0));
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
