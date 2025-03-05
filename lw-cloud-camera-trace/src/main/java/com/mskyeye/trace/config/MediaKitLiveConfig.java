package com.mskyeye.trace.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author nie
 * @date 2025/3/4 10:09
 */
@Data
@Component
@Slf4j
@ConfigurationProperties(prefix = "mediakit.live.config")
public class MediaKitLiveConfig {


    @PostConstruct
    public void init() {
        log.info("--------------------------------------------------");
        log.info("开始加载灭火无人机直播配置。。。。。。。。。。。。。。。。。。");
        log.info("服务器的ip：{}", this.ip);
        log.info("服务器的apiPort:{}", this.apiPort);
        log.info("服务器的webrtcPort:{}", this.webrtcPort);
        log.info("服务器的secret:{}", this.secret);
        log.info("服务器的vhost：{}", this.vhost);
        log.info("服务器的app：{}", this.app);
        log.info("录像时文件保存的位置：{}", this.customizedPath);
        log.info("--------------------------------------------------");
    }


    /**
     * ZLMediaKit 服务器的地址, ip
     */
    private String ip;


    /**
     * ZLMediaKit api 端口号
     */
    private Integer apiPort = 80;


    /**
     * webrtc 的播放端口
     */
    private Integer webrtcPort = 443;


    /**
     * rtmp 的流端口
     */
    private Integer rtmpPort = 1935;


    /**
     * 服务器访问secret
     */
    private String secret;


    /**
     * 虚拟主机
     */
    private String vhost = "__defaultVhost__";


    /**
     * app
     */
    private String app = "live";



    /**
     * 进行录像时，保存文件的位置
     */
    private String customizedPath = "/data/records";


    public String getCompleteUrl(String api) {
        return new StringBuilder("http://")
                .append(this.ip)
                .append(":")
                .append(apiPort)
                .append(api)
                .toString();
    }


    /**
     * 拼接适配webrtc拉流地址
     *
     * @return
     */
    public  String buildWebRtcUrl(String deviceSn) {
        StringBuilder streamUrl = new StringBuilder();
        String url = streamUrl.append("http://")
                .append(this.getIp())
                .append(":")
                .append(this.getWebrtcPort())
                .append("/index/api/webrtc")
                .append("?app=")
                .append(this.getApp())
                .append("&stream=")
                .append(deviceSn)
                .append("&type=play").toString();
        return url;
    }



    /**
     * 拼接推流地址
     * @return
     */
    public String buildRtmpUrl(String streamId){
        StringBuilder streamUrl = new StringBuilder();
        String rtmpUrl = streamUrl.append("rtmp://")
                .append(this.ip)
                .append(":")
                .append(this.rtmpPort)
                .append("/")
                .append(this.app)
                .append("/")
                .append(streamId)
                .toString();
        return rtmpUrl;
    }
}
