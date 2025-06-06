package com.mskyeye.trace.cron;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mskyeye.common.utils.StringUtil;
import com.mskyeye.trace.camera.hkws.callback.AlarmCallback;
import com.mskyeye.trace.camera.hkws.sdk.HkNetSDK;
import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.netty.control.GplCtrlTcpClientService;
import com.mskyeye.trace.netty.status.GplStatusTcpClientService;
import com.mskyeye.trace.proc.HkCameraProc;
import com.mskyeye.trace.proc.HpCameraProc;
import com.mskyeye.trace.utils.HkNetSDKLoader;
import com.mskyeye.trace.utils.RedisCache;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.mskyeye.trace.common.GlResources.*;

/**
 * @ClassName:QueryCameraInfoTask
 * @Description:查询和登录相机任务
 * @Author:R.Gong
 * @Date:2023/8/7 18:49
 * @Version:1.0
 **/
@Component
@EnableAsync
public class QueryAndLoginCameraTask {

    @Autowired
    public AlarmCallback callback;

    @Autowired
    private HpCameraProc hpCameraProc;

    @Autowired
    private HkCameraProc hkCameraProc;

    @Autowired
    private RedisCache redisCache;

    /**
     * 5s更新一次相机信息
     */
    @Scheduled(fixedDelay = 5000)
    @Async
    public void run() {
        try {
            //从Redis读取光电信息
            List<JSONObject> cacheList = redisCache.getCacheList(CAMERA_INFO_CACHE);
            ObjectMapper mapper = new ObjectMapper();
            List<YzCameraInfo> cacheCameraList = cacheList.stream()
                    .map(jsonObject -> mapper.convertValue(jsonObject, YzCameraInfo.class))
                    .collect(Collectors.toList());
            //相机更新、登录处理
            for (YzCameraInfo newCameraInfo : cacheCameraList) {
                Long cameraInfoId = newCameraInfo.getId();
                System.out.println("相机信息发生变化，重新加载："+GL_CameraInfoMap.get(114L));
                YzCameraInfo oldCameraInfo = GL_CameraInfoMap.get(cameraInfoId);
                if(!GL_CameraInfoMap.isEmpty()){
                    System.out.println("+++++++++++++++++++++++++++++++++"+oldCameraInfo);
                }
                //没有该相机或相机需要重新登录
                if (!GL_CameraInfoMap.containsKey(cameraInfoId) ||
                        (oldCameraInfo.getLoginInfo() != null && oldCameraInfo.getLoginInfo().equals("-1"))) {
                    if (newCameraInfo.getManu().equals("hp")) {
                        //如果有相机信息但无法登录，这里不做重新登录，交由定时任务reLoginHpCamera处理
                        if (GL_CameraInfoMap.containsKey(cameraInfoId)) {
                            continue;
                        }
                        //登录并获得相机token
                        String token = hpCameraProc.userLogin(newCameraInfo);
                        if (StringUtil.isEmpty(token)) {
                            token = "-1";
                        }
                        newCameraInfo.setLoginInfo(token);
                        //发送在线用户心跳
                        hpCameraProc.userOnlineHeart(newCameraInfo);
                    } else if (newCameraInfo.getManu().equals("hik")) {
                        newCameraInfo = hkCameraProc.hikLogin(newCameraInfo);
                    } else if (newCameraInfo.getManu().equals("dh")) {
//                        String url = "win32-x86-64/dh_lib/win/dhnetsdk.dll";
//                        //初始化dll、连接设备
//                        //TODO
//                        DhNetSDK dhNetSDK = Native.load(url, DhNetSDK.class);//调试时
////                        DhNetSDK dhNetSDK = Native.load("dhnetsdk", DhNetSDK.class);//打包时
//                        //初始化
//                        dhNetSDK.CLIENT_Init(new fDisConnectCB(),new NativeLong(0));
//                        // 设置断线重连回调接口，设置过断线重连成功回调函数后，当设备出现断线情况，SDK内部会自动进行重连操作
//                        // 此操作为可选操作，但建议用户进行设置
//                        dhNetSDK.CLIENT_SetAutoReconnect(new HaveReConnect(), new NativeLong(0));
//                        //设置登录超时时间和尝试次数，可选
//                        int waitTime = 5000; //登录请求响应超时时间设置为5S
//                        int tryTimes = 3;    //登录时尝试建立链接3次
//                        dhNetSDK.CLIENT_SetConnectTime(waitTime, tryTimes);
//                        //设备登录
//                        NET_DEVICEINFO_Ex netDeviceinfoEx = new NET_DEVICEINFO_Ex();
//                        int nError[] = {0};
//                        Long loginId = dhNetSDK.CLIENT_LoginEx2(newCameraInfo.getIp(), (short)newCameraInfo.getManPort().intValue(),
//                                newCameraInfo.getUserName(), newCameraInfo.getPassWord(),0, new Pointer(0),netDeviceinfoEx,nError);
//                        //存入登录ID
//                        newCameraInfo.setLoginInfo(String.valueOf(loginId));
//                        newCameraInfo.setDhNetSDK(dhNetSDK);
                    } else if (newCameraInfo.getManu().equals("gpl")) {
//                        String url = "win32-x86-64/gpl_lib/win/vsifsdk.dll";
//                        //初始化dll、连接设备
//                        //TODO
//                        GplNetSDK gplNetSDK = Native.load(url, GplNetSDK.class);//调试时
//                        //初始化
//                        gplNetSDK.VSIF_Init(new fDisConnectCB(), new NativeLong(0));
//                        // 设置断线重连回调接口，设置过断线重连成功回调函数后，当设备出现断线情况，SDK内部会自动进行重连操作
//                        // 此操作为可选操作，但建议用户进行设置
//                        gplNetSDK.VSIF_SetAutoReconnect(new HaveReConnect(), new NativeLong(0));
//                        //设置登录超时时间和尝试次数，可选
//                        int waitTime = 5000; //登录请求响应超时时间设置为5S
//                        int tryTimes = 3;    //登录时尝试建立链接3次
//                        gplNetSDK.VSIF_SetConnectTime(waitTime, tryTimes);
//                        //设备登录
//                        int nError[] = {0};
//                        Long loginId = gplNetSDK.VSIF_LoginEx2(newCameraInfo.getIp(), (short) newCameraInfo.getManPort().intValue(),
//                                newCameraInfo.getUserName(), newCameraInfo.getPassWord(), 0, new Pointer(0), 0, nError);
//                        //存入登录ID
//                        newCameraInfo.setLoginInfo(String.valueOf(loginId));
//                        newCameraInfo.setGplNetSDK(gplNetSDK);

                        //连接控制口
                        GplCtrlTcpClientService clientService1 = new GplCtrlTcpClientService();
                        clientService1.setCsAddr(newCameraInfo.getIp());
                        clientService1.setCsPort(Math.toIntExact(newCameraInfo.getSparePort1()));
                        clientService1.setCameraId(newCameraInfo.getId());
                        clientService1.createTcpConn();
                        //连接状态口
                        GplStatusTcpClientService clientService2 = new GplStatusTcpClientService();
                        clientService2.setCsAddr(newCameraInfo.getIp());
                        clientService2.setCsPort(Math.toIntExact(newCameraInfo.getSparePort2()));
                        clientService2.setCameraId(newCameraInfo.getId());
                        clientService2.createTcpConn();

                        newCameraInfo.setGplCtrlTcpClient(clientService1);
                        newCameraInfo.setGplStatusTcpClient(clientService2);
                        newCameraInfo.setLoginInfo(null);
                    }
                    GL_CameraInfoMap.put(cameraInfoId, newCameraInfo);
                    if(cameraInfoId ==114 ) {
                        System.out.println("相机信息加载：" + GL_CameraInfoMap.get(cameraInfoId) + "---" + newCameraInfo);
                    }
                    cancelTraceOrder(newCameraInfo);
                    GL_TraceInfoMap.remove(cameraInfoId);
                    continue;
                }
                //有该相机但需要重新登录(重要信息被修改),此时不需要回写跟踪信息和PTZ值
                if (!oldCameraInfo.equals(newCameraInfo)) {
                    if (newCameraInfo.getManu().equals("hp")) {
                        //登出
                        //hpCameraProc.userLogout(newCameraInfo);
                        //登录并获得相机token
                        String token = hpCameraProc.userLogin(newCameraInfo);
                        if (StringUtil.isEmpty(token)) {
                            return;
                        }
                        newCameraInfo.setLoginInfo(token);
                        //发送在线用户心跳
                        hpCameraProc.userOnlineHeart(newCameraInfo);
                    } else if (newCameraInfo.getManu().equals("hik")) {
                        //TODO
//                        String url = "win32-x86-64/hk_lib/win/HCNetSDK.dll";
                        HkNetSDK hkNetSDK = HkNetSDKLoader.loadSDK();

//                        String url = "/home/hk_lib/linux/libhcnetsdk.so";
                        //先做登出、清理操作
                        oldCameraInfo.getHkNetSDK().NET_DVR_Logout(new NativeLong(Long.valueOf(oldCameraInfo.getLoginInfo())));
                        oldCameraInfo.getHkNetSDK().NET_DVR_Cleanup();
                        //初始化dll、连接设备
                        //TODO
//                        HkNetSDK hkNetSDK = Native.load(url, HkNetSDK.class);//调试时
//                        HkNetSDK hkNetSDK = Native.load("HCNetSDK", HkNetSDK.class);//打包时
                        //相机登录
                        newCameraInfo = hkCameraProc.hikLogin(newCameraInfo);
                    } else if (newCameraInfo.getManu().equals("dh")) {
//                        String url = "win32-x86-64/dh_lib/win/dhnetsdk.dll";
//                        //先做登出、清理操作
//                        oldCameraInfo.getDhNetSDK().CLIENT_Logout(new NativeLong(Long.valueOf(oldCameraInfo.getLoginInfo())));
//                        oldCameraInfo.getDhNetSDK().CLIENT_Cleanup();
//                        //初始化dll、连接设备
//                        //TODO
//                        DhNetSDK dhNetSDK = Native.load(url, DhNetSDK.class);//调试时
////                        DhNetSDK dhNetSDK = Native.load("dhnetsdk", DhNetSDK.class);//打包时
//
//                        //初始化
//                        dhNetSDK.CLIENT_Init(new fDisConnectCB(),new NativeLong(0));
//                        // 设置断线重连回调接口，设置过断线重连成功回调函数后，当设备出现断线情况，SDK内部会自动进行重连操作
//                        // 此操作为可选操作，但建议用户进行设置
//                        dhNetSDK.CLIENT_SetAutoReconnect(new HaveReConnect(), new NativeLong(0));
//                        //设置登录超时时间和尝试次数，可选
//                        int waitTime = 5000; //登录请求响应超时时间设置为5S
//                        int tryTimes = 3;    //登录时尝试建立链接3次
//                        dhNetSDK.CLIENT_SetConnectTime(waitTime, tryTimes);
//                        //设备登录
//                        NET_DEVICEINFO_Ex netDeviceinfoEx = new NET_DEVICEINFO_Ex();
//                        int nError[] = {0};
//                        NativeLong loginId = dhNetSDK.CLIENT_LoginEx2(newCameraInfo.getIp(), newCameraInfo.getManPort().shortValue(),
//                                newCameraInfo.getUserName(), newCameraInfo.getPassWord(),0, null,netDeviceinfoEx,nError);
//                        //存入登录ID
//                        newCameraInfo.setLoginInfo(String.valueOf(loginId));
//                        newCameraInfo.setDhNetSDK(dhNetSDK);
                    } else if (newCameraInfo.getManu().equals("gpl")) {
//                        String url = "win32-x86-64/gpl_lib/win/vsifsdk.dll";
//                        //先做登出、清理操作
//                        oldCameraInfo.getGplNetSDK().VSIF_Logout(Long.valueOf(oldCameraInfo.getLoginInfo()));
//                        oldCameraInfo.getGplNetSDK().VSIF_Cleanup();
//                        //初始化dll、连接设备
//                        //TODO
//                        GplNetSDK gplNetSDK = Native.load(url, GplNetSDK.class);//调试时
//                        //初始化
//                        gplNetSDK.VSIF_Init(new fDisConnectCB(), new NativeLong(0));
//                        // 设置断线重连回调接口，设置过断线重连成功回调函数后，当设备出现断线情况，SDK内部会自动进行重连操作
//                        // 此操作为可选操作，但建议用户进行设置
//                        gplNetSDK.VSIF_SetAutoReconnect(new HaveReConnect(), new NativeLong(0));
//                        //设置登录超时时间和尝试次数，可选
//                        int waitTime = 5000; //登录请求响应超时时间设置为5S
//                        int tryTimes = 3;    //登录时尝试建立链接3次
//                        gplNetSDK.VSIF_SetConnectTime(waitTime, tryTimes);
//                        //设备登录
//                        int nError[] = {0};
//                        Long loginId = gplNetSDK.VSIF_LoginEx2(newCameraInfo.getIp(), (short) newCameraInfo.getManPort().intValue(),
//                                newCameraInfo.getUserName(), newCameraInfo.getPassWord(), 0, new Pointer(0), 0, nError);
//                        //存入登录ID
//                        newCameraInfo.setLoginInfo(String.valueOf(loginId));
//                        newCameraInfo.setGplNetSDK(gplNetSDK);


                        //连接控制口
                        GplCtrlTcpClientService clientService1 = oldCameraInfo.getGplCtrlTcpClient();
                        clientService1.setCsAddr(newCameraInfo.getIp());
                        clientService1.setCsPort(Math.toIntExact(newCameraInfo.getSparePort1()));
                        clientService1.setCameraId(newCameraInfo.getId());
//                        clientService1.close();
                        clientService1.createTcpConn();
                        //连接状态口
                        GplStatusTcpClientService clientService2 = oldCameraInfo.getGplStatusTcpClient();
                        clientService2.setCsAddr(newCameraInfo.getIp());
                        clientService2.setCsPort(Math.toIntExact(newCameraInfo.getSparePort2()));
                        clientService2.setCameraId(newCameraInfo.getId());
//                        clientService2.close();
                        clientService2.createTcpConn();

                        newCameraInfo.setGplCtrlTcpClient(clientService1);
                        newCameraInfo.setGplStatusTcpClient(clientService2);
                    }
                    GL_CameraInfoMap.put(cameraInfoId, newCameraInfo);
                    System.out.println("相机信息发生变化，重新加载："+GL_CameraInfoMap.get(cameraInfoId));
                    cancelTraceOrder(newCameraInfo);
                    GL_TraceInfoMap.remove(cameraInfoId);
                }
                //和普相机需要发送心跳包
                if (oldCameraInfo.getManu().equals("hp") && StringUtil.isNotEmpty(oldCameraInfo.getLoginInfo())) {
                    //发送在线用户心跳
                    hpCameraProc.userOnlineHeart(oldCameraInfo);
                }
                //高普乐相机断线重连机制
                if (oldCameraInfo.getManu().equals("gpl")) {
                    GplCtrlTcpClientService client1 = oldCameraInfo.getGplCtrlTcpClient();
                    if (client1.getChannel() == null || !client1.getChannel().isOpen()) {
//                        if(client1.getChannel() != null) {
//                            client1.close();
//                        }
                        client1.close();
                        client1.createTcpConn();
                    }
                    GplStatusTcpClientService client2 = oldCameraInfo.getGplStatusTcpClient();
                    if (client2.getChannel() == null || !client2.getChannel().isOpen()) {
//                        if(client2.getChannel() != null) {
//                            client2.close();
//                        }
                        client2.close();
                        client2.createTcpConn();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 取消跟踪
     *
     * @param yzCameraInfo
     * @return
     * @throws Exception
     */
    private void cancelTraceOrder(YzCameraInfo yzCameraInfo) throws Exception {

        if (yzCameraInfo.getManu().equals("hp")) {
            hpCameraProc.aziControl(yzCameraInfo, -1, true);
            hpCameraProc.boxTrackCtrl(yzCameraInfo, false, 1, 0, 0, 0, 0);
            hpCameraProc.photoTrackingCtrl(yzCameraInfo, false, 1);
        } else if (yzCameraInfo.getManu().equals("hik")) {
            //使用最基本的云台控制左移<停>指令结束跟踪
            yzCameraInfo.getHkNetSDK().NET_DVR_PTZControlWithSpeed_Other(new NativeLong(Long.valueOf(yzCameraInfo.getLoginInfo())),
                    new NativeLong(1L), 21, 1, 1);
        } else if (yzCameraInfo.getManu().equals("dh")) {

        } else if (yzCameraInfo.getManu().equals("gpl")) {

        }
    }
}
