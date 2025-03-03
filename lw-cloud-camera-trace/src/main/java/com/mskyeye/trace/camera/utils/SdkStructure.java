package com.mskyeye.trace.camera.utils;

import com.sun.jna.Structure;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName:SdkStructure
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/9/7 9:02
 * @Version:1.0
 **/
public class SdkStructure extends Structure {


    @Override
    protected List<String> getFieldOrder(){
        List<String> fieldOrderList = new ArrayList<String>();
        for (Class<?> cls = getClass();
             !cls.equals(SdkStructure.class);
             cls = cls.getSuperclass()) {
            Field[] fields = cls.getDeclaredFields();
            int modifiers;
            for (Field field : fields) {
                modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
                    continue;
                }
                fieldOrderList.add(field.getName());
            }
        }
        return fieldOrderList;
    }

    @Override
    public int fieldOffset(String name){
        return super.fieldOffset(name);
    }
}
