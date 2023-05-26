package com.mskyeye.common.annotation;



import com.mskyeye.common.annotation.validator.CheckLongitudeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName:CheckLatitude
 * @Description:检查经度合法性注解
 * @Author:R.Gong
 * @Date:2022/8/9 10:26
 * @Version:1.0
 **/
@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckLongitudeValidator.class) //校验的逻辑处理类
public @interface CheckLongitude {
    String message() default "请输入正确的纬度格式,精确到小数点后六位";   //提示的信息

    Class<?>[] groups() default { };  //分组验证，例如只在新增时进行校验等

    Class<? extends Payload>[] payload() default { };

}
