package com.mskyeye.trace.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.mskyeye.common.utils.BeanUtils;
import com.mskyeye.trace.config.MediaKitLiveApiConfig;
import com.mskyeye.trace.config.MediaKitLiveConfig;
import com.mskyeye.trace.model.LiveParamsDTO;
import com.mskyeye.trace.service.VideoStreamProxyService;
import lombok.extern.slf4j.Slf4j;
import org.geotools.ows.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.mskyeye.trace.constant.ThirdServiceConst;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author nie
 * @Description:
 * @create 2025/3/4 10:12
 */
@Service
@Slf4j
public class VideoStreamProxyServiceImpl implements VideoStreamProxyService {

    @Autowired
    private MediaKitLiveConfig liveConfig;

    @Override
    public String startStream(String url) throws ServiceException {
        if (StringUtils.isEmpty(url)) {
            throw new ServiceException("url参数不能为空");
        }
        String stream = null;
        String app = null;
        String[] parts = url.split("/");
// 过滤掉空字符串的情况（因为有可能前面有 rtsp://，会出现空段）
        List<String> validParts = Arrays.stream(parts)
                .filter(p -> p != null && !p.isEmpty())
                .collect(Collectors.toList());

        if (validParts.size() == 1) {
            // 只有一个名字，没有 app，用默认
            app = liveConfig.getApp();
            stream = validParts.get(0);
        } else if (validParts.size() >= 2) {
            // 至少有两层，取最后两层
            app = validParts.get(validParts.size() - 2);
            stream = validParts.get(validParts.size() - 1);
        } else {
            throw new ServiceException("url格式不正确: " + url);
        }

        LiveParamsDTO params = new LiveParamsDTO();
        params.setSecret(liveConfig.getSecret());
        params.setVhost(liveConfig.getVhost());
        params.setApp(app);
        params.setStream(stream);
        params.setUrl(url);//去除url的引号
        Map<String, Object> paramsMap = BeanUtils.convertToMap(params); // 将对象转化为map

        // 开启直播之前不管有没有拉流代理，先查询是否有该流的信息
        String checkStreamUrl = liveConfig.getCompleteUrl(MediaKitLiveApiConfig.LIVE_STREAM_INFO);
        log.info("发送请求，检查直播流的情况，请求地址为：{}，请求参数为：{}", checkStreamUrl, JSONObject.toJSONString(paramsMap));
        String checkResp = HttpUtil.get(checkStreamUrl, paramsMap);
        JSONObject checkRespJson = JSONObject.parseObject(checkResp);
        Integer checkRespCode = checkRespJson.getInteger(ThirdServiceConst.LiveConst.LIVE_CALLBACK_KEY);

        String streamProxyUrl = liveConfig.getCompleteUrl(MediaKitLiveApiConfig.ADD_STREAM_PROXY);
        if (Objects.equals(checkRespCode, ThirdServiceConst.LiveConst.NO_LIVE_STREAM_PROXY)) {
            // 说明没有流的代理信息，需要添加流的代理信息
            this.startLiveProxy(streamProxyUrl, paramsMap);
        } else if (Objects.equals(checkRespCode, ThirdServiceConst.LiveConst.LIVE_SUCCESS)) {
            // 说明查询流的代理信息成功，需要比对流代理信息是否和现在的相匹配
            if (!checkStreamProxyInfo(checkRespJson, url)) {
                // 若信息改动，则需要关闭代理，重新开启
                String delStreamProxy = liveConfig.getCompleteUrl(MediaKitLiveApiConfig.DEL_STREAM_PROXY);
                params.setKey(liveConfig.getVhost() + "/" + app + "/"+stream );
                String s = HttpUtil.get(delStreamProxy, BeanUtils.convertToMap(params));
                // 删除不用管状态，直接开启新的代理
                this.startLiveProxy(streamProxyUrl, paramsMap);
            }
        } else {
            throw new ServiceException(String.format("开启直播时出现未知错误：{%s}", checkResp));
        }
        // 拼接webrtc直播地址
        String otherUrl = liveConfig.buildWebRtcUrl(app,stream); // 获取直播的webrtc地址
        //拼接ws.flv播放地址
//        String otherUrl = liveConfig.buildFlvUrl(app,stream);
        return otherUrl;
    }

    /**
     * 开启直播代理
     */
    private void startLiveProxy(String streamProxyUrl, Map<String, Object> paramsMap) throws ServiceException {
        log.info("发送请求，开启直播，请求地址为：{},请求参数为：{}", streamProxyUrl, JSONObject.toJSONString(paramsMap));
        String resp = HttpUtil.get(streamProxyUrl, paramsMap);
        log.info("直播请求返回结果为：{}", resp);
        JSONObject jsonObject = JSONObject.parseObject(resp);
        Integer code = jsonObject.getInteger(ThirdServiceConst.LiveConst.LIVE_CALLBACK_KEY);
        if (!Objects.equals(code, ThirdServiceConst.LiveConst.LIVE_SUCCESS)) {
            throw new ServiceException("直播开启失败，请确认是否已经开启直播");
        }
    }
    /**
     * 检查流代理的信息
     *
     * @param jsonObject
     * @return
     */
    private Boolean checkStreamProxyInfo(JSONObject jsonObject, String streamUrl) {

        String app = jsonObject.getString(ThirdServiceConst.LiveConst.LIVE_APP_KEY);
        if (!Objects.equals(app, liveConfig.getApp())) {
            return false;
        }
        String originalUrl = jsonObject.getString(ThirdServiceConst.LiveConst.LIVE_ORIGINAL_STREAM_KEY);
        return Objects.equals(originalUrl, streamUrl);
    }
}
