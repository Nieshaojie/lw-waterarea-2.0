package com.mskyeye.trace.camera.hkws.callback;

import com.google.gson.Gson;
import com.mskyeye.trace.camera.hkws.struct.NET_DVR_SHIPSDETECTION_ALARM;
import com.mskyeye.trace.camera.hkws.struct.NET_VCA_DEV_INFO;
import com.mskyeye.trace.model.LwAiAlarmPacket;
import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.mq.utils.MqConnectionUtil;
import com.rabbitmq.client.AMQP;
import com.sun.jna.Pointer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.mskyeye.trace.common.GlResources.GL_CameraInfoMap;

/**
 * @ClassName:MyCallbackImpl
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/12/27 15:07
 * @Version:1.0
 **/
@Component
public class AlarmCallback implements MSGCallBack {

    @Autowired
    private MqConnectionUtil mqConnectionUtil;

    private AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
            .expiration("10000")
            .build();

    @Override
    public void invoke(int lCommand, Pointer pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser){
        switch (lCommand)
        {
            case 0x4521: //船只检测报警信息
            {

                NET_DVR_SHIPSDETECTION_ALARM struShipsDetection = new NET_DVR_SHIPSDETECTION_ALARM();

                Pointer sDetectInfo = struShipsDetection.getPointer();
                sDetectInfo.write(0, pAlarmInfo.getByteArray(-6, struShipsDetection.size()), 0, struShipsDetection.size());
                struShipsDetection.read();

                NET_VCA_DEV_INFO netVcaDevInfo = struShipsDetection.struDevInfo;
                String ip = new String(netVcaDevInfo.struDevIP.sIpV4).trim();
//                String ip = byteArrayToString(netVcaDevInfo.struDevIP.sIpV4);
                YzCameraInfo yzCameraInfo = new YzCameraInfo();
                for(Map.Entry<Long, YzCameraInfo> entry:GL_CameraInfoMap.entrySet()){
                    YzCameraInfo yzCameraInfo1 = entry.getValue();
                    if(ip.contains(yzCameraInfo1.getIp())){
                        yzCameraInfo = entry.getValue();
                        break;
                    }
                }
                if(yzCameraInfo.getIp() == null){
                    return;
                }
                LwAiAlarmPacket lwAiAlarmPacket = new LwAiAlarmPacket();
                lwAiAlarmPacket.setCMDTYPE("HIKALARM");
                lwAiAlarmPacket.setDEPTID(yzCameraInfo.getDeptId());
                lwAiAlarmPacket.setCAMERAID(yzCameraInfo.getId());
                lwAiAlarmPacket.setCAMERANAME(yzCameraInfo.getName());
                lwAiAlarmPacket.setLAT(yzCameraInfo.getLat().doubleValue());
                lwAiAlarmPacket.setLON(yzCameraInfo.getLon().doubleValue());
                lwAiAlarmPacket.setAIPOINTNAME(null);
                lwAiAlarmPacket.setEVENTTYPE(null);
                //发送给RabbitMQ
                try {
                    mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, "ai_alarm.key",
                            properties, new Gson().toJson(lwAiAlarmPacket).getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
    }

    private String byteArrayToString(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            if (b != 32) {
                sb.append((char) b);
            }
        }
        return sb.toString();
    }

    public String utf8ByteArrayToString(byte[] byteArray) {
        // 使用UTF-8字符集创建一个字符串
        return new String(byteArray, StandardCharsets.US_ASCII);
    }
}
