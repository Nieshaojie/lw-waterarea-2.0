package com.mskyeye.trace.annotation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @ClassName:BeijingTimeFormatAnnotationIntrospector
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/5/30 16:16
 * @Version:1.0
 **/
public class BeijingTimeFormatAnnotationIntrospector extends JacksonAnnotationIntrospector {

    @Override
    public JsonFormat.Value findFormat(Annotated a) {
        if (a.hasAnnotation(BeijingTimeFormat.class)) {
            JsonFormat.Value format = new JsonFormat.Value();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为北京时间
            format.withPattern(sdf.toPattern());
            return format;
        }
        return super.findFormat(a);
    }
}
