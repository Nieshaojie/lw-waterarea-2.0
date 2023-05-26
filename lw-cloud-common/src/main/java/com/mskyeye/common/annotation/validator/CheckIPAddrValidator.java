package com.mskyeye.common.annotation.validator;



import com.mskyeye.common.annotation.CheckIPAddr;
import com.mskyeye.common.utils.StringUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * @ClassName:CheckIPAddrValidator
 * @Description:IP地址校验器
 * @Author:R.Gong
 * @Date:2022/8/9 14:18
 * @Version:1.0
 **/
public class CheckIPAddrValidator implements ConstraintValidator<CheckIPAddr, String> {
    @Override
    public void initialize(CheckIPAddr myConstraint) {
        /* 初始化数据，如果有需要初始化的在此操作，没有则不需操作次函数 */
    }

    /**
     * 自定义校验逻辑
     * @param checkIPAddr
     * @param constraintValidatorContext
     * @return
     */
    @Override
    public boolean isValid(String checkIPAddr, ConstraintValidatorContext constraintValidatorContext) {
        /*  逻辑代码，不符合返回false，否则返回true */
        boolean flag = false;
        try {
            if (StringUtil.isNotEmpty(checkIPAddr)) {
                String source = checkIPAddr;
                Pattern pattern = Pattern.compile("^((25[0-5]|2[0-4]\\\\d|[1]{1}\\\\d{1}\\\\d{1}|[1-9]{1}\\\\d{1}|\\\\d{1})($|(?!\\\\.$)\\\\.)){4}$");
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
