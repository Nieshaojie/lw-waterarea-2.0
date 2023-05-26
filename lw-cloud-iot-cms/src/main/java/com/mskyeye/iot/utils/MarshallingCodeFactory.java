package com.mskyeye.iot.utils;

import io.netty.handler.codec.marshalling.DefaultMarshallerProvider;
import io.netty.handler.codec.marshalling.DefaultUnmarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallingDecoder;
import io.netty.handler.codec.marshalling.MarshallingEncoder;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

/**
 * @ClassName:MarshallingCodeFactory
 * @Description:编解码工具
 * @Author:R.Gong
 * @Date:2022/11/25 15:37
 * @Version:1.0
 **/
public final class MarshallingCodeFactory {
    /** 创建Jboss marshalling 解码器
     * @return*/
    public static io.netty.channel.ChannelHandler buildMarshallingDecoder() {
        //参数serial表示创建的是Java序列化工厂对象,由jboss-marshalling-serial提供
        MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory("serial");
        MarshallingConfiguration configuration = new MarshallingConfiguration();
        configuration.setVersion(5);
        DefaultUnmarshallerProvider provider = new DefaultUnmarshallerProvider(factory, configuration);
        return new MarshallingDecoder(provider, 1024);
    }

    /** 创建Jboss marshalling 编码器 */
    public static MarshallingEncoder buildMarshallingEncoder() {
        MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory("serial");
        MarshallingConfiguration configuration = new MarshallingConfiguration();
        configuration.setVersion(5);
        DefaultMarshallerProvider provider = new DefaultMarshallerProvider(factory, configuration);
        return new MarshallingEncoder(provider);
    }
}

