package com.mskyeye.trace.netty.status;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;

/**
 * @ClassName:StatusClientHandlerInitializer
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/4/25 17:13
 * @Version:1.0
 **/
public class StatusClientHandlerInitializer extends ChannelInitializer<SocketChannel> {

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
        // 高普乐光电状态推送固定12字节一帧，加解码器防止TCP粘包/拆包导致PT值丢失
        pipeline.addLast(new FixedLengthFrameDecoder(12));
        GplStatusClientHandler gplStatusClientHandler = new GplStatusClientHandler();
        pipeline.addLast(gplStatusClientHandler);
        this.setCtx(gplStatusClientHandler.getCtx());
        gplStatusClientHandler.setCameraId(this.cameraId);
    }
}