package com.mskyeye.trace.controller;

import com.alibaba.fastjson.JSONObject;
import com.mskyeye.trace.model.FishingDetectInfo;
import com.mskyeye.trace.utils.AjaxResult;
import com.mskyeye.trace.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.mskyeye.trace.common.GlResources.DETECT_KEY;
import static com.mskyeye.trace.common.GlResources.Gl_MonitorInfo;

/**
 * @ClassName:FishingDetectController
 * @Description:捕鱼识别控制器
 * @Author:R.Gong
 * @Date:2023/8/29 9:20
 * @Version:1.0
 **/
@RestController
@RequestMapping("/fishing_detect")
public class FishingDetectController {

    @Autowired
    private RedisCache redisCache;

    @PostMapping("/send_order")
    public AjaxResult send_order(@RequestBody FishingDetectInfo fishingDetectInfo){
        try {
            String str = JSONObject.toJSONString(fishingDetectInfo);
            System.out.println(str);
            redisCache.pushMsg(DETECT_KEY,JSONObject.toJSONString(fishingDetectInfo));

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String formattedDateTime = now.format(formatter);
//            System.out.println(formattedDateTime + "***************发送指令:"+ fishingDetectInfo);
            return AjaxResult.success();
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error();
        }
    }

    @GetMapping("/latest_detect_info")
    public AjaxResult latest_detect_info(){
        try {
            return AjaxResult.success(Gl_MonitorInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error();
        }
    }
}
