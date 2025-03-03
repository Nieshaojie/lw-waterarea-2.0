package com.mskyeye.iot.mq.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ClassName:ServerHeartbeatHandler
 * @Description:服务端心跳检查处理器
 * @Author:R.Gong
 * @Date:2022/11/25 16:52
 * @Version:1.0
 **/
@Component
@Slf4j
@ChannelHandler.Sharable
public class ServerHeartbeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg.equals("ping")){
            ctx.channel().writeAndFlush("pong");
        } else {
            //由下一个handler处理
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            //该事件需要配合 io.netty.handler.timeout.IdleStateHandler使用
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                //超过指定时间没有读事件,关闭连接
                log.info("超过心跳时间,关闭和服务端的连接:{}", ctx.channel().remoteAddress());
                ctx.channel().close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("远程用户关闭TCP连接:{}", ctx.channel().remoteAddress());
        ctx.channel().close();
    }
}