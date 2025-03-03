package com.mskyeye.trace.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @ClassName:PostRequestUtil
 * @Description:Post请求工具
 * @Author:R.Gong
 * @Date:2023/8/1 14:22
 * @Version:1.0
 **/
public class PostRequestUtil {

    public static String sendToHpPostReq(String ip, String port, String requestBody) {
        // 创建HttpClient对象
        HttpClient httpClient = HttpClients.createDefault();

        String url = "http://" + ip + ":" + port + "/cgi-bin/proc.cgi";

        // 创建HttpPost对象，设置请求URL
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)    // 连接目标url超时时间
                .setSocketTimeout(5000)     // 等待数据响应超时时间
                .build();
        // 设置请求头
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        // 设置请求体参数
        StringEntity requestEntity = new StringEntity(requestBody, "UTF-8");
        httpPost.setEntity(requestEntity);
        httpPost.setConfig(requestConfig);
        // 发送POST请求
        try {
            HttpResponse response = httpClient.execute(httpPost);
            // 处理响应
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity responseEntity = response.getEntity();
                String responseBody = EntityUtils.toString(responseEntity);
                return responseBody;
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }
}
