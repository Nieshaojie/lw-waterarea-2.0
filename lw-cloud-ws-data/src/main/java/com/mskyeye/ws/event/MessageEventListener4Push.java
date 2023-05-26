package com.mskyeye.ws.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @ClassName:MessageEventListener4Push
 * @Description:事件监听者，向指定websocket会话推送消息
 * @Author:R.Gong
 * @Date:2022/7/20 11:08
 * @Version:1.0
 **/

@Component
@Slf4j
public class MessageEventListener4Push {

    @EventListener
    public void handleEvent(MessageEvent event) throws JsonProcessingException {
        String payload = event.getMsg();
        log.info("Message received in WebSocket: {}", payload);

//        Power power = new ObjectMapper().readValue(payload, Power.class);
//        String city = power.getCity();
//        Session session = WebSocketSession.getSessionByClient(city);
//        if (null != session) {
//            WebSocketSession.sendMessage2Target(session, payload);
//        }
    }
}
