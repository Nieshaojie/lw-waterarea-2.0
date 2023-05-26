package com.mskyeye.common.exception;


import com.mskyeye.common.utils.JsonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @ClassName:CustomExceptionHandler
 * @Description:异常处理类
 * @Author:R.Gong
 * @Date:2022/7/19 9:20
 * @Version:1.0
 **/

@ControllerAdvice
public class CustomExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonData handle(Exception e) {

        logger.error("[ 系统异常 ]{}", e.getMessage());

        if (e instanceof MyException) {
            MyException myException = (MyException) e;
            return JsonData.buildError(myException.getCode(), myException.getMsg());
        } else {
            return JsonData.buildError("全局异常，未知错误");
        }
    }
}
