package com.mskyeye.lwradarstationdata.protocol.utils.serializer;

/**
 * @ClassName:OyzFloatSerialize
 * @Description:将double类型的数据格式化成小数点后两位的字符串数据
 * @Author:R.Gong
 * @Date:2023/3/20 14:49
 * @Version:1.0
 **/

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * 用户将double类型的数据格式化成小数点后两位的字符串数据：如输出为“900.00”.
 */
public class OyzFloatSerialize extends JsonSerializer<Float> {

    private DecimalFormat df = new DecimalFormat("#0.00");

    public OyzFloatSerialize() {
    }

    @Override
    public void serialize(Float o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, IOException {
        if(o.toString() != null) {
            Float dd = Float.parseFloat(o.toString());
            jsonGenerator.writeString(df.format(dd));
        } else{
            jsonGenerator.writeString(o.toString());
        }
    }
}
