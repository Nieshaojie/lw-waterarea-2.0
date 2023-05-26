package com.mskyeye.lwradarstationdata.protocol.radar.spx;

import lombok.Data;

/**
 * @ClassName:SpxPacketHeader
 * @Description:Spx雷达数据包头
 * @Author:R.Gong
 * @Date:2022/11/2 16:08
 * @Version:1.0
 **/
@Data
public class SpxPacketHeader {

    private short PacketHeader_Flag; //0x4342

    private short PacketHeader_Type; //0x0115：过正北时发送的航迹状态报文， 0x0112:发送的Extended的航迹报文

    private int PacketHeader_Size;  //包含前面16字节的报文长度

    private int PacketHeader_Secs;  //1970-1-1 0:0:0开始的秒数

    private int PacketHeader_Usecs; //微秒数

}
