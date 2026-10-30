package com.mskyeye.trace.netty.control;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;

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
        // 高普乐控制口响应固定8字节一帧，加解码器防止TCP粘包/拆包导致强光状态丢失
        pipeline.addLast(new FixedLengthFrameDecoder(8));
        GplCtrlClientHandler gplCtrlClientHandler = new GplCtrlClientHandler();
        pipeline.addLast(gplCtrlClientHandler);
        gplCtrlClientHandler.setCameraId(this.cameraId);
    }
}