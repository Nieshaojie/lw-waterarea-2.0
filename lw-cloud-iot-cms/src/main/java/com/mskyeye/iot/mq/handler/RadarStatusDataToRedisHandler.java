package com.mskyeye.iot.mq.handler;

import com.mskyeye.iot.utils.RedisCache;
import com.mskyeye.lwradarstationdata.protocol.radar.custom.RadarRealInfo;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName:RadarStatusDataToRedisHandler
 * @Description:雷达状态数据处理
 * @Author:R.Gong
 * @Date:2023/5/31 19:00
 * @Version:1.0
 **/
@Component
@Slf4j
@ChannelHandler.Sharable
public class RadarStatusDataToRedisHandler extends ChannelInboundHandlerAdapter {

    private static final String RADAR_STATE_BY_DEVICE = "yz2.0_info:yz_radar_state:";

    @Autowired
    private RedisCache redisCache;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RadarRealInfo) {
            RadarRealInfo rtp = (RadarRealInfo) msg;
            //存入Redis中,且存活时间为10s
            redisCache.setCacheObject(RADAR_STATE_BY_DEVICE + rtp.getRadarCode()
                    ,rtp.getRadarStatus()==1?"在线":"离线",10, TimeUnit.SECONDS);
        }else {
            //抛弃该消息
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
