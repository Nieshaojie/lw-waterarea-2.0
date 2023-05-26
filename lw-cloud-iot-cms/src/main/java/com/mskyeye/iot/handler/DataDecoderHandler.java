package com.mskyeye.iot.handler;

import com.alibaba.fastjson.JSON;
import com.mskyeye.lwradarstationdata.protocol.radar.custom.TrackTcpPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * @ClassName:DataDecoderHandler
 * @Description:数据解析处理器
 * @Author:R.Gong
 * @Date:2022/11/2 14:04
 * @Version:1.0
 **/

@Slf4j
public class DataDecoderHandler extends MessageToMessageDecoder<ByteBuf> {
//    private static String clientIpAllowed = "36.7.81.242";  //该编码器只试用于指定客户端传入的数据，因为协议是根据客户端数据类型定义的

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        String client = ctx.channel().remoteAddress().toString();
        //判断IP合法性
//        if (StringUtil.isNotEmpty(client) && client.contains(clientIpAllowed)) {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            TrackTcpPacket rtp = JSON.parseObject(new String(bytes, CharsetUtil.UTF_8), TrackTcpPacket.class);
            if(null != rtp){
                //rtp.setTLastUpdatetime(System.currentTimeMillis());
                out.add(rtp);
            }
//        } else {
//            log.error("...............服务端暂不接收该IP数据...............");
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("...............数据解析异常...............");
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * ByteBuf转String
     *
     * @param buf
     * @return
     * @throws UnsupportedEncodingException
     */
    public String convertByteBufToString(ByteBuf buf) throws UnsupportedEncodingException {
        String str;
        if (buf.hasArray()) { // 处理堆缓冲区
            str = new String(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
        } else { // 处理直接缓冲区以及复合缓冲区
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            str = new String(bytes, 0, buf.readableBytes());
        }
        return str;
    }
}
