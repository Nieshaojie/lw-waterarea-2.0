package com.mskyeye.common.exception;


/**
 * @ClassName:MyException
 * @Description:自定义异常类
 * @Author:R.Gong
 * @Date:2022/7/19 11:20
 * @Version:1.0
 **/

public class MyException extends RuntimeException{

    private Integer code;

    private String msg;

    public MyException(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;

    }
}
