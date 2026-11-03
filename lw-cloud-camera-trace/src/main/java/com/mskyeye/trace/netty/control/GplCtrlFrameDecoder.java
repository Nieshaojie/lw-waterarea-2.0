package com.mskyeye.trace.netty.control;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 高普乐光电控制端口帧解码器
 * <p>
 * 协议帧格式: 固定 8 字节, 帧头 A7 01 01 01 (强光状态响应)
 * <pre>
 *  Byte 0: A7        帧头固定
 *  Byte 1: 01        帧头固定
 *  Byte 2: 01        帧头固定
 *  Byte 3: 01        帧头固定 (强光控制)
 *  Byte 4: 07        查询指令码
 *  Byte 5: 82        响应标识
 *  Byte 6: 00/FF     强光开关状态
 *  Byte 7: 校验和
 * </pre>
 * 设计同 GplStatusFrameDecoder: 扫描帧头再切帧, 自动跳过非帧头数据, 粘包自愈
 */
public class GplCtrlFrameDecoder extends ByteToMessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(GplCtrlFrameDecoder.class);

    /** 帧头: A7 01 01 01 (强光状态相关响应) */
    private static final byte[] HEADER = {(byte) 0xA7, 0x01, 0x01, 0x01};

    /** 完整帧长度 */
    private static final int FRAME_LEN = 8;

    /** 保留最后 MAX_KEEP 字节防止帧头跨包 */
    private static final int MAX_KEEP = HEADER.length - 1;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        while (in.readableBytes() >= FRAME_LEN) {
            int headerOffset = findHeader(in);
            if (headerOffset < 0) {
                int discard = in.readableBytes() - MAX_KEEP;
                if (discard > 0) {
                    in.skipBytes(discard);
                    log.debug("GplCtrlFrameDecoder 未找到帧头, 丢弃 {} 字节", discard);
                }
                return;
            }

            if (headerOffset > 0) {
                log.debug("GplCtrlFrameDecoder 跳过 {} 字节非帧头数据", headerOffset);
                in.skipBytes(headerOffset);
            }

            if (in.readableBytes() < FRAME_LEN) {
                return;
            }

            ByteBuf frame = in.readRetainedSlice(FRAME_LEN);
            out.add(frame);
        }
    }

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
