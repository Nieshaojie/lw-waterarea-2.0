package com.mskyeye.trace.controller;

import com.mskyeye.trace.service.VideoStreamProxyService;
import com.mskyeye.trace.utils.AjaxResult;
import io.swagger.annotations.ApiOperation;
import org.geotools.ows.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 拉流代理
 * @author nie
 * @create 2025/3/4 9:52
 */
@RestController
@RequestMapping(value = "/video/proxy")
public class VideoStreamProxyController {

    @Autowired
    private VideoStreamProxyService videoStreamProxyService;

    @GetMapping(value = "/start/stream")
    @ApiOperation(value = "开启rtsp拉流代理")
    public AjaxResult startFireDroneStream(@RequestParam String url) throws ServiceException {
        String s = videoStreamProxyService.startStream(url);
        return AjaxResult.success(s);
    }

}
