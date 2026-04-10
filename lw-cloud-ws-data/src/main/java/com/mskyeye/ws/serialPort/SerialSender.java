package com.mskyeye.ws.serialPort;

import com.fazecast.jSerialComm.SerialPort;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.OutputStream;

@Component
public class SerialSender {

    private static final String PORT = "/dev/ttyUSB0";
    private static final int BAUD = 57600;

    private SerialPort serialPort;
    private OutputStream outputStream;

    @PostConstruct
    public void init() {
        try {
            serialPort = SerialPort.getCommPort(PORT);
            serialPort.setComPortParameters(BAUD, 8, 1, SerialPort.NO_PARITY);
            serialPort.openPort();
            outputStream = serialPort.getOutputStream();
            System.out.println("✅ 数传串口打开成功：/dev/ttyS0");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFrame(byte[] frame) {
        try {
            if (outputStream != null) {
                outputStream.write(frame);
                outputStream.flush();
                System.out.println("✅ 雷达引导帧发送成功 length=" + frame.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}