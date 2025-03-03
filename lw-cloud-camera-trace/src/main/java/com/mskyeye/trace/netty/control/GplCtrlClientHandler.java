package com.mskyeye.trace.netty.control;

/**
 * @ClassName:GplCtrlClientHandler
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/4/25 15:58
 * @Version:1.0
 **/

import com.mskyeye.trace.common.GlResources;
import com.mskyeye.trace.model.YzCameraInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;

/**
 * 高普乐相机控制端口处理器类
 */
public class GplCtrlClientHandler extends ChannelDuplexHandler {

    private Long cameraId;

    private static final byte[] closeLightOrder = {(byte) 0xA7,(byte) 0x01,(byte) 0x01,(byte) 0x01,
            (byte) 0x06,(byte) 0x02,(byte) 0x00,(byte) 0x0B};

    public Long getCameraId() {
        return cameraId;
    }

    public void setCameraId(Long cameraId) {
        this.cameraId = cameraId;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        byte[] array = (byte[]) msg;
        ByteBuf buf = Unpooled.wrappedBuffer(array); // 将byte数组包装成ByteBuf
        ctx.write(buf, promise); // 写入数据
        // 判断引用计数是否大于0再释放
        if (buf.refCnt() > 0) {
            ReferenceCountUtil.release(buf); // 手动释放ByteBuf
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        ByteBuf byteBuf = (ByteBuf) msg;
        try {
            // 创建一个和 ByteBuf 大小相同的 byte 数组
            int len = byteBuf.readableBytes();
            byte[] bytes = new byte[len];

            // 将 ByteBuf 的内容读取到 byte 数组中
            byteBuf.readBytes(bytes);
            //强光状态信息
            if(len == 8 && (bytes[0] & 0xFF) == 0xA7 && (bytes[1] & 0xFF) == 0x01
                    && (bytes[2] & 0xFF) == 0x01 && (bytes[3] & 0xFF) == 0x01
                    && (bytes[4] & 0xFF) == 0x07 && (bytes[5] & 0xFF) == 0x82){
                YzCameraInfo yzCameraInfo = GlResources.GL_CameraInfoMap.get(cameraId);
                //未开启
                if((bytes[6] & 0xFF) == 0x00 && (bytes[7] & 0xFF) == 0x8C){
                    yzCameraInfo.setIsLightOpen(0);
                }
                //已开启
                else if((bytes[6] & 0xFF) == 0xFF && (bytes[7] & 0xFF) == 0x8B){
                    //强光开启后,30s自动关闭
                    if(yzCameraInfo.getIsLightOpen() == 0){
                        ScheduledFuture<?> future = ctx.executor().schedule(() -> {
                            // 关闭强光指令
                            yzCameraInfo.getGplCtrlTcpClient().sendInfo(closeLightOrder);
                        }, 30, TimeUnit.SECONDS);
                    }
                    yzCameraInfo.setIsLightOpen(1);
                }
                GlResources.GL_CameraInfoMap.put(cameraId, yzCameraInfo);
            }
        }catch (Exception e){
            byteBuf.release();
            throw new RuntimeException(e);
        }
        byteBuf.release();
    }

    //异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}
