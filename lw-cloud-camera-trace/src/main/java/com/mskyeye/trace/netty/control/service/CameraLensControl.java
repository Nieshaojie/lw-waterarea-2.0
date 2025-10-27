package com.mskyeye.trace.netty.control.service;

import com.mskyeye.trace.model.YzCameraInfo;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * PELCO-DV1.7 镜头控制接口
 * 定义镜头变倍、聚焦、光圈的基础控制动作
 */
public interface CameraLensControl {

    /** 镜头变倍+（视场角变小） */
    void zoomIn(YzCameraInfo camera,String type) throws Exception;

    /** 镜头变倍-（视场角变大） */
    void zoomOut(YzCameraInfo camera,String type) throws Exception;

    /** 镜头聚焦+（焦距变大） */
    void focusNear(YzCameraInfo camera,String type) throws Exception;

    /** 镜头聚焦-（焦距变小） */
    void focusFar(YzCameraInfo camera,String type) throws Exception;

    /** 镜头光圈+（视场角变小） */
    void irisClose(YzCameraInfo camera,String type) throws Exception;

    /** 镜头光圈-（视场角变大） */
    void irisOpen(YzCameraInfo camera,String type) throws Exception;

    /** 所有动作停止（停止变倍、聚焦、光圈） */
    void stop(YzCameraInfo camera, String type) throws Exception;

    /** 开启 AI 自动跟踪 */
    void startAiTrack(YzCameraInfo camera) throws Exception;

    /** 关闭 AI 自动跟踪 */
    void stopAiTrack(YzCameraInfo camera) throws Exception;

    /**云台控制接口*/
    void controlPTZ(YzCameraInfo camera, String type, String direction, int speed)  throws Exception;

    /**云台停止*/
    void stopPTZ(YzCameraInfo camera, String type) throws Exception;

}
