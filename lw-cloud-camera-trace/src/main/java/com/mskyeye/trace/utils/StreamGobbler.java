package com.mskyeye.trace.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @ClassName:StreamGobbler
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/5/23 12:39
 * @Version:1.0
 **/
public class StreamGobbler implements Runnable {

    private final InputStream inputStream;
    private final String type;

    public StreamGobbler(InputStream inputStream, String type) {
        this.inputStream = inputStream;
        this.type = type;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(type + " > " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}