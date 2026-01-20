package com.mskyeye.iot.tcp;

import com.mskyeye.iot.mq.handler.AisStaticDataToMqHandler;
import com.mskyeye.iot.mq.handler.RadarStatusDataToRedisHandler;
import com.mskyeye.iot.mq.handler.ServerHeartbeatHandler;
import com.mskyeye.iot.mq.handler.TrackToMqHandler;
import com.mskyeye.iot.mq.util.MqConnectionUtil;
import com.mskyeye.iot.utils.IpUtil;
import com.mskyeye.iot.utils.MarshallingCodeFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @ClassName:CSDataService
 * @Description:中心站数据服务器
 * @Author:R.Gong
 * @Date:2022/10/22 17:30
 * @Version:1.0
 **/
@Component
@Slf4j
@RefreshScope
public class CSDataService implements ApplicationRunner {

    @Value("${server_port}")
    private Integer serverPort;

    @Autowired
    private MqConnectionUtil mqConnUtil;

    @Autowired
    private ServerHeartbeatHandler serverHeartbeatHandler;

    @Autowired
    private TrackToMqHandler trackToMqHandler;

    @Autowired
    private AisStaticDataToMqHandler aisStaticDataToMqHandler;

    @Autowired
    private RadarStatusDataToRedisHandler radarStatusDataToRedisHandler;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("服务端启动中");
        //连接RabbitMQ
        mqConnUtil.initMqConfig();
        //配置服务端线程池,主从线程池
        //其中：用于Acceptor的主"线程池"以及用于I/O工作的从"线程池"
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            //配置启动引导类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)//设置服务端通道
                    .localAddress(serverPort)// 绑定监听端口
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_REUSEADDR, true) //快速复用端口
                    .childOption(ChannelOption.TCP_NODELAY,true);//无延迟

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() { // 绑定客户端连接时候触发操作
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    log.info("-------------有客户端连接--------------");
                    /*log.info("IP:" + ch.localAddress().getHostName());
                    log.info("Port:" + ch.localAddress().getPort());
                    log.info("IP详情:" + IpUtil.getIpVo(ch.localAddress().getHostName()));*/

//                    ch.pipeline().addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));
                    ch.pipeline().addLast(MarshallingCodeFactory.buildMarshallingEncoder());
                    ch.pipeline().addLast(MarshallingCodeFactory.buildMarshallingDecoder());
                    ch.pipeline().addLast(serverHeartbeatHandler);
                    ch.pipeline().addLast(trackToMqHandler);  //航迹处理
                    ch.pipeline().addLast(aisStaticDataToMqHandler);  //AIS静态数据处理
                    ch.pipeline().addLast(radarStatusDataToRedisHandler);  //雷达状态数据处理
                }
            });

            ChannelFuture cf = serverBootstrap.bind().sync(); // 服务器异步创建绑定
            log.info("TCP服务端启动成功，监听端口:{}",cf.channel().localAddress());
            cf.channel().closeFuture().sync(); //关闭服务器通道
        } catch (Exception e) {
            e.printStackTrace();
            log.error("【中心站信令服务】【异常信息】" + e.getMessage());
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
