package com.mskyeye.common.utils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @ClassName:JsonStateSerializer
 * @Description:状态序列化
 * @Author:R.Gong
 * @Date:2022/7/28 15:31
 * @Version:1.0
 **/
@Component
public class JsonStateSerializer extends JsonSerializer<Integer> {

    @Override
    public void serialize(Integer stateType, JsonGenerator jsonGenerator,
                          SerializerProvider provider) throws IOException, JsonProcessingException {
        String value;
        switch (stateType) {
            case -1:
                value = "过期";
                break;
            case 0:
                value = "离线";
                break;
            case 1:
                value = "在线";
                break;
            default:
                value = "错误";
                break;
        }
        jsonGenerator.writeString(value);
    }
}
