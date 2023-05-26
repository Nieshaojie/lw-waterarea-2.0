package com.mskyeye.ws.server;


import com.mskyeye.common.utils.StringUtil;
import com.mskyeye.ws.common.GlobalResources;
import com.mskyeye.ws.utils.WebSocketSession;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.yeauty.annotation.*;
import org.yeauty.pojo.Session;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

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

    @BeforeHandshake
    public void handshake(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap) {
//        session.setSubprotocols("stomp");
//        //添加public后缀的可以绕过鉴权功能
//        if(("public").equals(arg)){
//            return;
//        }
//        String accessToken = headers.get("token");
//        if (accessToken == null) {
//            System.out.println("认证失败!");
//            session.close();
//        }
//        if (StringUtils.isNotBlank(accessToken)) {
//            Claims claims = JWTUtils.checkJWT(accessToken);
//            if (claims == null) {
//                System.out.println("登录过期!");
//                session.close();
//            }
//        }
    }

    @OnOpen
    public void onOpen(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap) throws Exception {
        System.out.println("【new connection】: " + arg);
        WebSocketSession.addClient(session, arg);
        System.out.println(req);
        log.info("有新连接加入！当前在线客户端数量：{}", WebSocketSession.getOnlineCount());
//        sendTrackBufferMsg(session);//发送缓存航迹数据给新用户
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        String client = WebSocketSession.getClientBySession(session);
        System.out.println("【one connection closed】：" + client);
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
        System.out.println("[new message]: " + message);
        session.sendText("Hello Netty!");
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

    @PostConstruct
    public void run(){
        new Thread(() -> {
            while (true) {
                try {
                    if(!GlobalResources.curTrackQueue.isEmpty()){
                        sendTrackMsgToAll(GlobalResources.curTrackQueue.poll());
                    }
                } catch (Exception e) {
                    log.error("【<向所有用户发送数据>失败】:", e);
                }
//                try {
//                    Thread.sleep(1L);
//                } catch (InterruptedException e) {
//                    log.error("【<向所有用户发送数据>定时器错误】:", e);
//                }
            }
        }).start();
    }

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
                WebSocketSession.sendMessage2All(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
