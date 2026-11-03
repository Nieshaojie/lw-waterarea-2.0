package com.mskyeye.trace.netty.status;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 高普乐光电状态端口帧解码器
 * <p>
 * 协议帧格式: 固定 12 字节, 帧头 A7 01 02 09
 * <pre>
 *  Byte 0: A7        帧头固定
 *  Byte 1: 01        帧头固定
 *  Byte 2: 02        帧头固定
 *  Byte 3: 09        帧头固定 (方位+俯仰状态推送)
 *  Byte 4: ?
 *  Byte 5: ?
 *  Byte 6: ?
 *  Byte 7-8: 方位值 iCurVal (大端)
 *  Byte 9-10: 俯仰值 iTVal (大端)
 *  Byte 11: ?
 * </pre>
 * <p>
 * 设计要点:
 * 1. 不是 FixedLengthFrameDecoder 那种刚性切帧, 而是每次扫描帧头 A7 01 02 09 再切 12 字节
 * 2. 粘包(一次收多帧) → while 循环一次扫出所有完整帧 ✅
 * 3. 混包(非12字节数据混入) → 扫描时自动跳过, 从下一个帧头重新对齐 ✅
 * 4. 拆包(不够12字节) → 返回等下次, cumulation buffer 保留已有数据 ✅
 * 5. 帧错位自愈 → 永远在找帧头, 永远能自动重对齐 ✅
 */
public class GplStatusFrameDecoder extends ByteToMessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(GplStatusFrameDecoder.class);

    /** 帧头: A7 01 02 09 (方位+俯仰状态推送) */
    private static final byte[] HEADER = {(byte) 0xA7, 0x01, 0x02, 0x09};

    /** 完整帧长度 = 帧头(4) + 数据(8) */
    private static final int FRAME_LEN = 12;

    /**
     * 丢弃缓冲区尾部的前缀不完整帧头字节后, 最多保留多少字节等下次 channelRead
     * 保留 HEADER.length - 1 = 3 字节, 防止帧头被拆到两次 channelRead 里
     */
    private static final int MAX_KEEP = HEADER.length - 1;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 循环扫描, 一次性处理掉所有能凑成完整帧的数据
        while (in.readableBytes() >= FRAME_LEN) {
            // 从当前 readerIndex 开始扫描帧头
            int headerOffset = findHeader(in);
            if (headerOffset < 0) {
                // 当前缓冲区里没有找到帧头, 丢弃掉不可能构成完整帧头的字节
                // 保留最后 MAX_KEEP 字节: 防止帧头跨两次 channelRead
                int discard = in.readableBytes() - MAX_KEEP;
                if (discard > 0) {
                    in.skipBytes(discard);
                    log.debug("GplStatusFrameDecoder 未找到帧头, 丢弃 {} 字节", discard);
                }
                return;
            }

            // 跳过帧头之前的"垃圾字节" (非 A7 01 02 09 开头的数据)
            if (headerOffset > 0) {
                log.debug("GplStatusFrameDecoder 跳过 {} 字节非帧头数据", headerOffset);
                in.skipBytes(headerOffset);
            }

            // 此时 readerIndex 正好对准帧头, 检查是否够一帧
            if (in.readableBytes() < FRAME_LEN) {
                return; // 不够, cumulation buffer 保留已有数据, 等下次 channelRead
            }

            // 切出完整 12 字节帧 (retained slice: 引用计数+1, 后续 handler 负责 release)
            ByteBuf frame = in.readRetainedSlice(FRAME_LEN);
            out.add(frame);
        }

        // 缓冲区剩余数据不够 FRAME_LEN, 等下次 channelRead 继续累积
    }

    /**
     * 从当前 readerIndex 开始扫描, 返回第一个帧头 A7 01 02 09 相对 readerIndex 的偏移量
     * 没找到返回 -1
     */
    private int findHeader(ByteBuf in) {
        int scanEnd = in.readableBytes() - HEADER.length + 1;
        for (int i = 0; i < scanEnd; i++) {
            int pos = in.readerIndex() + i;
            boolean match = true;
            for (int j = 0; j < HEADER.length; j++) {
                if (in.getByte(pos + j) != HEADER[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return i;
            }
        }
        return -1;
    }
}
