package com.mskyeye.trace.netty.status;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

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
        // 自定义帧头扫描解码器: 扫描 A7 01 02 09 帧头再切 12 字节
        // 相比 FixedLengthFrameDecoder 的优势: 自动跳过非PT数据、粘包自愈、帧错位后自动重对齐
        pipeline.addLast(new GplStatusFrameDecoder());
        GplStatusClientHandler gplStatusClientHandler = new GplStatusClientHandler();
        pipeline.addLast(gplStatusClientHandler);
        this.setCtx(gplStatusClientHandler.getCtx());
        gplStatusClientHandler.setCameraId(this.cameraId);
    }
}