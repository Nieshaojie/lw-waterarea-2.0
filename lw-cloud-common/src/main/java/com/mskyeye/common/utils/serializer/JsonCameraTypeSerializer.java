package com.mskyeye.common.utils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @ClassName:JsonCameraTypeSerializer
 * @Description:相机类型解析
 * @Author:R.Gong
 * @Date:2021/7/12 11:00
 * @Version:1.0
 **/
public class JsonCameraTypeSerializer extends JsonSerializer<Integer> {
    @Override
    public void serialize(Integer cameraType, JsonGenerator jsonGenerator,
                          SerializerProvider provider) throws IOException, JsonProcessingException {
        String value;
        switch (cameraType) {
            case 1:
                value = "固定枪机";
                break;
            case 2:
                value = "球机";
                break;
            case 3:
                value = "卡口摄像机";
                break;
            case 4:
                value = "有云台枪机";
                break;
            case 5:
                value = "半球-固定摄像机";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + cameraType);
        }
        jsonGenerator.writeString(value);
    }
}
