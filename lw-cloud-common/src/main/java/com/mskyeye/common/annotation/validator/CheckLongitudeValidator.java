package com.mskyeye.common.annotation.validator;



import com.mskyeye.common.annotation.CheckLongitude;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * @ClassName:CheckLongitudeValidator
 * @Description:经度校验器
 * @Author:R.Gong
 * @Date:2022/8/9 10:34
 * @Version:1.0
 **/
public class CheckLongitudeValidator implements ConstraintValidator<CheckLongitude, BigDecimal> {
    @Override
    public void initialize(CheckLongitude myConstraint) {
        /* 初始化数据，如果有需要初始化的在此操作，没有则不需操作次函数 */
    }

    /**
     * 自定义校验逻辑
     * @param checkLongitude
     * @param constraintValidatorContext
     * @return
     */
    @Override
    public boolean isValid(BigDecimal checkLongitude, ConstraintValidatorContext constraintValidatorContext) {
        /*  逻辑代码，不符合返回false，否则返回true */
        boolean flag = false;
        try {
            if (checkLongitude != null) {
                String source = checkLongitude.toString();
                Pattern pattern = Pattern.compile("((?:[0-9]|[1-9][0-9]|1[0-7][0-9]|180)\\.([0-9]{6}))");
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
