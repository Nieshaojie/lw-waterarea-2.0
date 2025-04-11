package com.mskyeye.ws.server;


import com.alibaba.fastjson.JSON;
import com.mskyeye.common.utils.StringUtil;
import com.mskyeye.lwradarstationdata.protocol.track.Content;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;
import com.mskyeye.ws.common.GlobalResources;
import com.mskyeye.ws.model.DeviceInDept;
import com.mskyeye.ws.model.LwAiAlarmPacket;
import com.mskyeye.ws.model.LwCameraStatusPacket;
import com.mskyeye.ws.model.LwCarInfoPacket;
import com.mskyeye.ws.redis.utils.RedisCache;
import com.mskyeye.ws.utils.AlarmInfoSender;
import com.mskyeye.ws.utils.WebSocketSession;
import com.ruoyi.common.core.domain.model.LoginUser;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.yeauty.annotation.*;
import org.yeauty.pojo.Session;

import java.io.IOException;
import java.util.*;

import static com.mskyeye.ws.common.GlobalResources.deviceInfoMap;
import static com.mskyeye.ws.common.GlobalResources.sessionKV;

/**
 * @ClassName:WebSocketServer
 * @Description:websocket服务
 * @Author:R.Gong
 * @Date:2022/7/20 10:57
 * @Version:1.0
 **/

@ServerEndpoint(path = "/lw/ws/{arg}", port = "8090")
@Component
@Slf4j
public class WebSocketServer {

    /**
     * 登录用户 redis key
     */
    private static final String LOGIN_TOKEN_KEY = "login_tokens:";

    @Autowired
    private RedisCache redisCache;

    @BeforeHandshake
    public void handshake(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap) {
        session.setSubprotocols("stomp");
        //添加public后缀的可以绕过鉴权功能
//        if(("public").equals(arg)){
//            return;
//        }
        String[] reqArray = arg.split("\\$");
        String userName = reqArray[0];
        String token = reqArray[1];
        //该账号免密连接ws
        if(userName.equals("ycsy_portal")){
            return;
        }
        Map<String,LoginUser> map = queryLoginInfo();//查询用户信息
        if (token == null || !map.containsKey(token)
                || !(map.get(token).getUser().getUserName()).contains(userName)) {
            System.out.println("认证失败!");
            session.close();
        }
    }

    @OnOpen
    public void onOpen(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap) throws Exception {
        String[] reqArray = arg.split("\\$");
        String userName = reqArray[0];
        String token = reqArray[1];
        System.out.println("【new connection】: " + userName);
        //该账号免密连接ws,展示admin账号资源
        if(userName.equals("ycsy_portal")){
            WebSocketSession.addClient(session, deviceInfoMap.get(100L));
        }else{
            Map<String,LoginUser> map = queryLoginInfo();//查询用户信息
            if(deviceInfoMap != null && !deviceInfoMap.isEmpty()){
                LoginUser loginUser = map.get(token);
                WebSocketSession.addClient(session, deviceInfoMap.get(loginUser.getDeptId()));
            }
        }
        System.out.println(req);
        log.info("有新连接加入！当前在线客户端数量：{}", WebSocketSession.getOnlineCount());
//        sendTrackBufferMsg(session);//发送缓存航迹数据给新用户
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        System.out.println("【one connection closed】：" + session);
        WebSocketSession.removeClient(session);
        log.info("有一连接关闭！当前在线客户端数量：{}", WebSocketSession.getOnlineCount());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        session.close();
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(Session session, String message) {
//        System.out.println("[new message]: " + message);
//        session.sendText("Hello Netty!");
    }

    @OnBinary
    public void onBinary(Session session, byte[] bytes) {
        for (byte b : bytes) {
            System.out.println(b);
        }
        session.sendBinary(bytes);
    }

    @OnEvent
    public void onEvent(Session session, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                    System.out.println("read idle");
                    break;
                case WRITER_IDLE:
                    System.out.println("write idle");
                    break;
                case ALL_IDLE:
                    System.out.println("all idle");
                    break;
                default:
                    break;
            }
        }
    }

//    @PostConstruct
//    public void run(){
//        new Thread(() -> {
//            while (true) {
//                try {
//                    if(!GlobalResources.curTrackQueue.isEmpty()){
//                        sendTrackMsgToAll(GlobalResources.curTrackQueue.take());
//                    }
//                } catch (Exception e) {
//                    log.error("【<向所有用户发送数据>失败】:", e);
//                }
//            }
//        }).start();
//    }

    /**
     * 向刚连接的用户发送缓存历史航迹
     * @param session
     * @return
     * @throws Exception
     */
    private boolean sendTrackBufferMsg(Session session)throws Exception{
        for(String trackMsg:GlobalResources.capTrackQueue){
            WebSocketSession.sendMessage2Target(session,trackMsg);
        }
        return true;
    }
    /**
     * 向所有用户发送航迹数据
     */
    public void sendTrackMsgToAll(String msg) {
        try {
            if(StringUtil.isNotEmpty(msg)){
                //解析成航迹数据对象
                LwTrackPacket lwTrackPacket = JSON.parseObject(msg, LwTrackPacket.class);
                Content cnt = lwTrackPacket.getITEM().get(0);
                Set<Session> keys = sessionKV.keySet();
                synchronized (keys) {
                    for (Session session : keys) {
                        DeviceInDept devices = sessionKV.get(session);
                        List<Long> radarIdList = devices.getRadarIdList();
                        if (radarIdList.contains(Long.valueOf(cnt.getSTATIONID()))) {
                            session.sendText(msg);
                            if(StringUtil.isNotEmpty(cnt.getALARM())) {
                                    AlarmInfoSender.sendAlarmInfo(cnt);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 向所有用户发送相机状态数据
     */
    public void sendCameraStatusToAll(String msg) {
        try {
            if(StringUtil.isNotEmpty(msg)){
                //解析成相机状态数据对象
                LwCameraStatusPacket lwCameraStatusPacket = JSON.parseObject(msg, LwCameraStatusPacket.class);
                Set<Session> keys = sessionKV.keySet();
                synchronized (keys) {
                    for (Session session : keys) {
                        DeviceInDept devices = sessionKV.get(session);
                        List<Long> cameraIdList = devices.getCameraIdList();
                        if (cameraIdList.contains(lwCameraStatusPacket.getIPCID())) {
                            session.sendText(msg);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 向所有用户发送AI告警事件数据
     */
    public void sendAiAlarmEventToAll(String msg) {
        try {
            if(StringUtil.isNotEmpty(msg)){
                //解析成AI告警事件数据对象
                LwAiAlarmPacket lwAiAlarmPacket = JSON.parseObject(msg, LwAiAlarmPacket.class);
                Set<Session> keys = sessionKV.keySet();
                synchronized (keys) {
                    for (Session session : keys) {
                        DeviceInDept devices = sessionKV.get(session);
                        List<Long> cameraIdList = devices.getCameraIdList();
                        if (cameraIdList.contains(lwAiAlarmPacket.getCAMERAID())) {
                            session.sendText(msg);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 向所有用户发送车载GPS数据
     */
    public void sendCarInfoToAll(String msg) {
        try {
            if(StringUtil.isNotEmpty(msg)){
                LwCarInfoPacket lwCarInfoPacket = JSON.parseObject(msg, LwCarInfoPacket.class);
                Set<Session> keys = sessionKV.keySet();
                synchronized (keys) {
                    for (Session session : keys) {
                        DeviceInDept devices = sessionKV.get(session);
                        if (lwCarInfoPacket.getDEPTID() == devices.getDeptId()) {
                            session.sendText(msg);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 从Redis中查询用户信息
     * @return
     */
    private Map<String,LoginUser> queryLoginInfo(){
        Map<String,LoginUser> map = new HashMap<>();
        Collection<String> keys = redisCache.keys(LOGIN_TOKEN_KEY + "*");
        for (String key : keys)
        {
            LoginUser user = redisCache.getCacheObject(key);
            if(user != null){
                map.put(user.getToken(),user);
            }
        }
        return map;
    }

}
