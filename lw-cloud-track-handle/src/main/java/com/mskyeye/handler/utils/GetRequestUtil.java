package com.mskyeye.handler.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.Map;

/**
 * @ClassName:GetRequestUtil
 * @Description:车载GPS的HTTP请求工具类
 * @Author:R.Gong
 * @Date:2023/10/22 20:22
 * @Version:1.0
 **/
public class GetRequestUtil {

    public static String sendToGpsGetReq(String method, Map<String, String> params) {
        // 创建 HttpClient 对象
        HttpClient httpClient = HttpClients.createDefault();

        try {
            URIBuilder uriBuilder = new URIBuilder("http://api.588gps.net/GetDateServices.asmx/" + method);

            // 添加参数
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }

            // 创建 HttpGet 对象，设置请求 URL
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)    // 连接目标url超时时间
                    .setConnectionRequestTimeout(5000)// 请求超时时间
                    .setSocketTimeout(5000)     // 等待数据响应超时时间
                    .build();
            // 设置请求头
            httpGet.setConfig(requestConfig);

            // 发送 GET 请求
            HttpResponse response = httpClient.execute(httpGet);
            // 处理响应
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity responseEntity = response.getEntity();
                String responseBody = EntityUtils.toString(responseEntity);
                return responseBody;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
