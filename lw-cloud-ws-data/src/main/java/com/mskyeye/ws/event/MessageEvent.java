package com.mskyeye.ws.event;

import org.springframework.context.ApplicationEvent;

/**
 * @ClassName:MessageEvent
 * @Description:事件发布者
 * @Author:R.Gong
 * @Date:2022/7/21 14:54
 * @Version:1.0
 **/
public class MessageEvent extends ApplicationEvent {

    private String msg;

    public MessageEvent(Object source, String msg) {
        super(source);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
