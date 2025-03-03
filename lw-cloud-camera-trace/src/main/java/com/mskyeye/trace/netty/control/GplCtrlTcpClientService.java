package com.mskyeye.trace.netty.control;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ResourceLeakDetector;

import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName:GplCtrlTcpClientService
 * @Description:高普乐相机控制端口TCP客户端
 * @Author:R.Gong
 * @Date:2024/4/22 15:01
 * @Version:1.0
 **/

public class GplCtrlTcpClientService {

    private String csAddr;//ip地址
    private Integer csPort;//端口号
    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private Channel clientChannel;
    private ChannelHandlerContext ctx;
    private Long cameraId;

    public String getCsAddr() {
        return csAddr;
    }

    public void setCsAddr(String csAddr) {
        this.csAddr = csAddr;
    }

    public Integer getCsPort() {
        return csPort;
    }

    public void setCsPort(Integer csPort) {
        this.csPort = csPort;
    }

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

    public boolean createTcpConn() {
        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        CtrlClientHandlerInitializer client = new CtrlClientHandlerInitializer();
        client.setCameraId(this.cameraId);
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .handler(client);
        connect();
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
        this.setCtx(client.getCtx());
        return true;
    }

    public boolean connect() {
        try {
            ChannelFuture channelFuture = bootstrap.connect(csAddr, csPort);
            boolean notTimeout = channelFuture.awaitUninterruptibly(3000, TimeUnit.SECONDS);
            clientChannel = channelFuture.channel();
            if (notTimeout) {
                if (clientChannel != null && clientChannel.isActive()) {
                    return true;
                }
                Throwable cause = channelFuture.cause();
                if (cause != null) {
                    exceptionHandler(cause);
                }
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        clientChannel.close();
        return false;
    }

    private void exceptionHandler(Throwable cause) {
        if (cause instanceof ConnectException) {
        } else if (cause instanceof ClosedChannelException) {
        } else {
        }
    }

    public void close() {
        if (clientChannel != null) {
            clientChannel.close();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    public Channel getChannel() {
        return clientChannel;
    }

    public boolean sendInfo(Object obj){
        if (clientChannel != null && clientChannel.isActive()) {
            clientChannel.writeAndFlush(obj);
            return true;
        }
        return false;
    }
}
