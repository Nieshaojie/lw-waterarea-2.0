package com.mskyeye.trace.config;

/**
 * @author nie
 * @date 2025/3/4 10:09
 */
public class MediaKitLiveApiConfig {

    /**
     * 添加流代理api
     */
    public static final String ADD_STREAM_PROXY = "/index/api/addStreamProxy";


    /**
     * 删除拉流代理
     */
    public static final String DEL_STREAM_PROXY = "/index/api/delStreamProxy";

    /**
     * webrtc访问的api
     *
     */
    public static final String LIVE_RESP_WEBRTC = "/index/api/webrtc";


    /**
     * 获取直播流的信息
     */
    public static final String LIVE_STREAM_INFO = "/index/api/getMediaInfo";


    /**
     * 直播录制
     */
    public static final String LIVE_STREAM_RECORD = "/index/api/startRecord";

    /**
     * 停止直播录制
     */
    public static final String LIVE_STREAM_RECORD_STP = "/index/api/stopRecord";


    /**
     * 获取直播流的录制状态
     */
    public static final String LIVE_STREAM_RECORD_STATUS = "/index/api/isRecording";


}
