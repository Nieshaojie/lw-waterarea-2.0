package com.mskyeye.trace.netty.status;

/**
 * @ClassName:GplStatusClientHandler
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/4/25 15:59
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * 高普乐相机状态端口处理器类
 */

public class GplStatusClientHandler extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(GplStatusClientHandler.class);

    private ChannelHandlerContext ctx;

    private Long cameraId;

    private Long num = 0L;//计数器

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
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        ByteBuf byteBuf = (ByteBuf) msg;
        try {
            // FixedLengthFrameDecoder(12) 已经保证每次收到恰好 12 字节
            int len = byteBuf.readableBytes();
            byte[] bytes = new byte[len];

            // 将 ByteBuf 的内容读取到 byte 数组中
            byteBuf.readBytes(bytes);

            //方位、俯仰值解析 (字头 A7 01 02 09)
            if (len == 12 && (bytes[0] & 0xFF) == 0xA7 && (bytes[1] & 0xFF) == 0x01
                    && (bytes[2] & 0xFF) == 0x02 && (bytes[3] & 0xFF) == 0x09) {
                YzCameraInfo yzCameraInfo = GlResources.GL_CameraInfoMap.get(cameraId);
                if (yzCameraInfo == null) {
                    log.warn("GL_CameraInfoMap 中找不到 cameraId={}, 跳过本次PT值更新", cameraId);
                    return;
                }

                Double curPval = null, curTval = null;

                // ========== 方位值 ==========
                // 水平osd计算方法（修正了与零点值比较的BUG）：
                // 1：现在位置＜零点，-(现在位置-零点位置)*比值
                // 2：现在位置＞零点，360-(现在位置-零点位置)*比值
                // 3：现在位置=零点，零
                if (yzCameraInfo.getAziMultiply() != null && yzCameraInfo.getAziZeroVal() != null) {
                    Integer iCurVal = (((bytes[7] & 0xFF) << 8) + (bytes[8] & 0xFF));
                    Double dCurVal = 0.0D;
                    // 修正：应该与 aziZeroVal（零点值）比较，之前错误地与 aziMultiply（倍率）比较
                    if (iCurVal > yzCameraInfo.getAziZeroVal()) {
                        dCurVal = 360.0D - (iCurVal - yzCameraInfo.getAziZeroVal()) * yzCameraInfo.getAziMultiply();
                    } else if (iCurVal < yzCameraInfo.getAziZeroVal()) {
                        dCurVal = (yzCameraInfo.getAziZeroVal() - iCurVal) * yzCameraInfo.getAziMultiply();
                    }
                    curPval = (360.0 + yzCameraInfo.getAngle() + dCurVal) % 360.0;
                } else {
                    log.debug("相机 {} aziMultiply 或 aziZeroVal 为 null, 跳过P值更新", yzCameraInfo.getName());
                }

                // ========== 俯仰值 ==========
                // 俯仰osd计算方法：
                // 1：现在位置≠零点，-(现在位置-零点位置)*比值
                // 2：现在位置=零点，零
                if (yzCameraInfo.getPitchMultiply() != null && yzCameraInfo.getPitchZeroVal() != null) {
                    Integer iTVal = ((bytes[9] & 0xFF) << 8) + (bytes[10] & 0xFF);
                    curTval = yzCameraInfo.getPitchMultiply() *
                            (yzCameraInfo.getPitchZeroVal() - iTVal);
                } else {
                    log.debug("相机 {} pitchMultiply 或 pitchZeroVal 为 null, 跳过T值更新", yzCameraInfo.getName());
                }

                if (curPval != null) {
                    yzCameraInfo.setCurPVal(doubleFormate(2, curPval));
                }
                if (curTval != null) {
                    yzCameraInfo.setCurTVal(doubleFormate(2, curTval));
                }
                GlResources.GL_CameraInfoMap.put(cameraId, yzCameraInfo);
                log.debug("光电状态推送更新PT, cameraId={}, curP={}, curT={}",
                        cameraId, yzCameraInfo.getCurPVal(), yzCameraInfo.getCurTVal());
            }
        }catch (Exception e){
            log.error("GplStatusClientHandler channelRead 异常, cameraId={}", cameraId, e);
            byteBuf.release();
        }
        byteBuf.release();
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

    //与服务器建立连接
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        setCtx(ctx);
        log.info("高普乐相机状态口已连接, cameraId={}, remote={}", cameraId, ctx.channel().remoteAddress());
    }

    //异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("高普乐相机状态口异常, cameraId={}", cameraId, cause);
        ctx.channel().close();
    }


    private void printByteInfo(byte[] bytes) {
        // 将 byte 数组转换为十六进制字符串（大写形式）
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF).toUpperCase();
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
            hexString.append(" ");
        }

        String hexStringResult = hexString.toString();

        System.out.println("Uppercase Hexadecimal String: " + hexStringResult);
    }

    /**
     * double保留几位有效数字。num：位数，src：源数据
     *
     * @param num
     * @param src
     * @return
     */
    private double doubleFormate(int num, double src) {
        BigDecimal bg = new BigDecimal(src);
        double result = bg.setScale(num, BigDecimal.ROUND_HALF_UP).doubleValue();
        return result;
    }
}
