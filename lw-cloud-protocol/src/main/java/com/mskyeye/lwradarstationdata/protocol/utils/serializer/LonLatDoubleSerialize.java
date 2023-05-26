package com.mskyeye.lwradarstationdata.protocol.utils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * @ClassName:LonLatDoubleSerialize
 * @Description:经纬度序列化
 * @Author:R.Gong
 * @Date:2023/2/20 15:03
 * @Version:1.0
 **/
public class LonLatDoubleSerialize extends JsonSerializer<Double> {

    private DecimalFormat df = new DecimalFormat("#0.0000000");

    public LonLatDoubleSerialize() {
    }

    @Override
    public void serialize(Double o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, IOException {
        if(o.toString() != null) {
            Double dd = Double.parseDouble(o.toString());
            jsonGenerator.writeString(df.format(dd));
        } else{
            jsonGenerator.writeString(o.toString());
        }
    }
}
