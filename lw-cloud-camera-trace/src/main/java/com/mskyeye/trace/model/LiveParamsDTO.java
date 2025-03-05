package com.mskyeye.trace.model;

import lombok.Data;

/**
 * @author huangkun
 * @date 2023/5/9 14:29
 *
 * 直播参数DTO
 */
@Data
public class LiveParamsDTO {


    /**
     * api操作密钥(配置文件配置)
     */
    private String secret;

    /**
     *  添加的流的虚拟主机，例如__defaultVhost__
     */
    private String vhost;

    /**
     * 添加的流的应用名，例如live
     */
    private String app;

    /**
     *添加的流的id名，例如test
     */
    private String stream;

    /**
     * 拉流地址，例如rtmp://live.hkstv.hk.lxdns.com/live/hks2
     */
    private String url;

    /**
     * 是否转rtmp/flv协议
     */
    private Integer enable_rtmp = 1;


    /**
     * 拉流次数，默认三次
     */
    private Integer retry_count = 1;


    /**
     * schema
     */
    private String schema = "rtsp";


    /**
     * 流的完整key
     */
    private String key;


    /**
     * 类型 0/1
     * 默认为1
     */
    private Integer type = 1;


    /**
     * 录像保存的位置
     */
    private String customized_path;



    /**
     * mp4录像切片时间大小,单位秒，置0则采用配置项
     * 默认为0
     */
    private Integer max_second = 0;
}
