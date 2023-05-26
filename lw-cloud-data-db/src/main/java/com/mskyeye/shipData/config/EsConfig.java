package com.mskyeye.shipData.config;

/**
 * @ClassName:EsConfig
 * @Description:elasticsearch配置类
 * @Author:R.Gong
 * @Date:2022/12/21 10:09
 * @Version:1.0
 **/

import lombok.Data;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ES builder
 */
@Configuration
@RefreshScope
@Data
public class EsConfig {

    @Value("${es_ip}")
    private String esAddr;

    @Value("${es_port}")
    private String esPort;

    public RestClientBuilder restClientBuilder;

    public RestHighLevelClient restHighLevelClient;

    /**
     * es restful client builder
     *
     * @return restful client
     */
    @Bean
    public Boolean restClientBuilder() {
        // 设置IP
        HttpHost esHost = new HttpHost(esAddr, Integer.parseInt(esPort));

        RestClientBuilder restClientBuilder = RestClient.builder(esHost);
//        setPassword(restClientBuilder);
//        setTImeout(restClientBuilder);

        this.restClientBuilder = restClientBuilder;
        this.restHighLevelClient = new RestHighLevelClient(restClientBuilder);
        return true;
    }

    /**
     * 设置超时时间
     */
    private void setTImeout(RestClientBuilder restClientBuilder) {
        restClientBuilder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder builder) {
                return builder.setConnectTimeout(1000)
                        .setSocketTimeout(1000);
            }
        });
    }

    /**
     * 设置ES密码
     */
    private void setPassword(RestClientBuilder restClientBuilder) {
        // 设置密码
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("userName", "password"));


        restClientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                return httpAsyncClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setDefaultIOReactorConfig(
                                IOReactorConfig.custom()
                                        .setIoThreadCount(4)
                                        .build()
                        );
            }
        });
    }
}
