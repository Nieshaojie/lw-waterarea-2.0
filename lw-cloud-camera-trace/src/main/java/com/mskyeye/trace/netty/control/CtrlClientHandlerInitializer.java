package com.mskyeye.trace.netty.control;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @ClassName:CtrlClientHandlerInitializer
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/4/25 17:16
 * @Version:1.0
 **/
public class CtrlClientHandlerInitializer extends ChannelInitializer<SocketChannel> {

    private ChannelHandlerContext ctx;
    private Long cameraId;

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public Long getCameraId() {
        return cameraId;
    }

    public void setCameraId(Long cameraId) {
        this.cameraId = cameraId;
    }
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        GplCtrlClientHandler gplCtrlClientHandler = new GplCtrlClientHandler();
        pipeline.addLast(gplCtrlClientHandler);
        gplCtrlClientHandler.setCameraId(this.cameraId);
    }
}