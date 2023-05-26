package com.mskyeye.common.annotation.validator;



import com.mskyeye.common.annotation.CheckLatitude;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * @ClassName:ConstraintValidator
 * @Description:纬度校验器
 * @Author:R.Gong
 * @Date:2022/8/9 10:27
 * @Version:1.0
 **/
public class CheckLatitudeValidator implements ConstraintValidator<CheckLatitude, BigDecimal> {
    @Override
    public void initialize(CheckLatitude myConstraint) {
        /* 初始化数据，如果有需要初始化的在此操作，没有则不需操作次函数 */
    }

    /**
     * 自定义校验逻辑
     * @param checkLatitude
     * @param constraintValidatorContext
     * @return
     */
    @Override
    public boolean isValid(BigDecimal checkLatitude, ConstraintValidatorContext constraintValidatorContext) {
        /*  逻辑代码，不符合返回false，否则返回true */
        boolean flag = false;
        try {
            if (checkLatitude != null) {
                String source = checkLatitude.toString();
                Pattern pattern = Pattern.compile("((?:[0-9]|[1-8][0-9]|90)\\.([0-9]{6}))");
                if (pattern.matcher(source).matches()) {
                    flag = true;
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
        return flag;
    }
}
