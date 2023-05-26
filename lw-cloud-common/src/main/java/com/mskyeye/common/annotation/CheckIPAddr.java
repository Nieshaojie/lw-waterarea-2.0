package com.mskyeye.common.annotation;

import com.mskyeye.common.annotation.validator.CheckIPAddrValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName:CheckIPAddr
 * @Description:检查IP合法性注解
 * @Author:R.Gong
 * @Date:2022/8/9 14:17
 * @Version:1.0
 **/
@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckIPAddrValidator.class) //校验的逻辑处理类
public @interface CheckIPAddr {

    String message() default "请输入正确的IP格式";   //提示的信息

    Class<?>[] groups() default {};  //分组验证，例如只在新增时进行校验等

    Class<? extends Payload>[] payload() default {};
}

