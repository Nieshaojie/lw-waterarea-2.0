package com.mskyeye.common.utils.serializer;

/**
 * @ClassName:JsonDateSerializer
 * @Description:Date类型转换成可显示的字符串类型
 * @Author:R.Gong
 * @Date:2020/7/4 18:29
 * @Version:1.0
 **/

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * json 的时间的转换
 *
 */
@Component
public class JsonDateSerializer extends JsonSerializer<Date> {
    private SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator,
                          SerializerProvider provider) throws IOException, JsonProcessingException {
        String value = dateFormat.format(date);
        jsonGenerator.writeString(value);
    }
}
