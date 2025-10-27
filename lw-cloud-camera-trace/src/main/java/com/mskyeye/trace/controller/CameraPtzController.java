package com.mskyeye.trace.controller;

import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.netty.control.service.CameraLensControl;
import com.mskyeye.trace.utils.AjaxResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.mskyeye.trace.common.GlResources.GL_CameraInfoMap;

/**
 * @Description: 云台与镜头控制接口（基于 PELCO-DV1.7 协议）
 * @author nie
 * @create 2025/10/20
 */
@RestController
@RequestMapping("/camera/control")
public class CameraPtzController {

    @Autowired
    private CameraLensControl lensControl;

    @ApiOperation(value = "镜头变倍+（视场角变小）")
    @GetMapping("/zoom/in")
    public AjaxResult zoomIn(@RequestParam Long cameraId, @RequestParam String type) throws Exception {
        YzCameraInfo camera = GL_CameraInfoMap.get(cameraId);
        lensControl.zoomIn(camera, type);
        return AjaxResult.success();
    }

    @ApiOperation(value = "镜头变倍-（视场角变大）")
    @GetMapping("/zoom/out")
    public AjaxResult zoomOut(@RequestParam Long cameraId, @RequestParam String type) throws Exception {
        YzCameraInfo camera = GL_CameraInfoMap.get(cameraId);
        lensControl.zoomOut(camera, type);
        return AjaxResult.success();
    }

    @ApiOperation(value = "镜头聚焦+（焦距变大）")
    @GetMapping("/focus/near")
    public AjaxResult focusNear(@RequestParam Long cameraId, @RequestParam String type) throws Exception {
        YzCameraInfo camera = GL_CameraInfoMap.get(cameraId);
        lensControl.focusNear(camera, type);
        return AjaxResult.success();
    }

    @ApiOperation(value = "镜头聚焦-（焦距变小）")
    @GetMapping("/focus/far")
    public AjaxResult focusFar(@RequestParam Long cameraId, @RequestParam String type) throws Exception {
        YzCameraInfo camera = GL_CameraInfoMap.get(cameraId);
        lensControl.focusFar(camera, type);
        return AjaxResult.success();
    }

    @ApiOperation(value = "镜头光圈+（视场角变小）")
    @GetMapping("/iris/close")
    public AjaxResult irisClose(@RequestParam Long cameraId, @RequestParam String type) throws Exception {
        YzCameraInfo camera = GL_CameraInfoMap.get(cameraId);
        lensControl.irisClose(camera, type);
        return AjaxResult.success();
    }

    @ApiOperation(value = "镜头光圈-（视场角变大）")
    @GetMapping("/iris/open")
    public AjaxResult irisOpen(@RequestParam Long cameraId, @RequestParam String type) throws Exception {
        YzCameraInfo camera = GL_CameraInfoMap.get(cameraId);
        lensControl.irisOpen(camera, type);
        return AjaxResult.success();
    }

    @ApiOperation(value = "停止所有动作")
    @GetMapping("/stop")
    public AjaxResult stop(@RequestParam Long cameraId, @RequestParam String type) throws Exception {
        YzCameraInfo camera = GL_CameraInfoMap.get(cameraId);
        lensControl.stop(camera, type);
        return AjaxResult.success();
    }

    @ApiOperation(value = "开启ai")
    @GetMapping("/ai/start")
    public AjaxResult start(@RequestParam Long cameraId) throws Exception {
        YzCameraInfo camera = GL_CameraInfoMap.get(cameraId);
        lensControl.startAiTrack(camera);
        return AjaxResult.success();
    }

    @ApiOperation(value = "开启ai")
    @GetMapping("/ai/stop")
    public AjaxResult stop(@RequestParam Long cameraId) throws Exception {
        YzCameraInfo camera = GL_CameraInfoMap.get(cameraId);
        lensControl.stopAiTrack(camera);
        return AjaxResult.success();
    }

    @ApiOperation(value = "云台控制（方向+速度）")
    @GetMapping("/pzt/control")
    public AjaxResult controlPTZ(@RequestParam Long cameraId,
                                 @RequestParam String type,
                                 @RequestParam String direction,
                                 @RequestParam int speed) throws Exception {
        YzCameraInfo camera = GL_CameraInfoMap.get(cameraId);
        lensControl.controlPTZ(camera, type, direction, speed);
        return AjaxResult.success();
    }

    @ApiOperation(value = "云台停止")
    @GetMapping("/pzt/stop")
    public AjaxResult stopPTZ(@RequestParam Long cameraId,
                              @RequestParam String type) throws Exception {
        YzCameraInfo camera = GL_CameraInfoMap.get(cameraId);
        lensControl.stopPTZ(camera, type);
        return AjaxResult.success();
    }
}
