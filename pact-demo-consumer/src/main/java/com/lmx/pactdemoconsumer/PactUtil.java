package com.lmx.pactdemoconsumer;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;

@Slf4j
public class PactUtil {
    public static ThreadLocal pactHolder = new ThreadLocal();

    /**
     * 以对象来构建期望的请求或者响应
     * <p>
     * 定义格式规范：如日期,
     * 正则匹配等
     * </p>
     *
     * @param o
     * @param pactDslJsonBody
     */
    public static PactDslJsonBody buildJson(PactDslJsonBody pactDslJsonBody, Object o) {
        for (Field field : o.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                String name = field.getType().getName();
                if (!ifObject(name) && field.getType().isInstance(new String())) {
                    pactDslJsonBody.stringMatcher(field.getName(), "^[A-Za-z0-9.@\\u4e00-\\u9fa5]+$", (String) field.get(o));
                } else if (!ifObject(name) && (field.getType().isInstance(new Integer(1))
                        || field.getType().isInstance(new Long(1))
                        || field.getType().isInstance(new BigDecimal(1)))) {
                    pactDslJsonBody.numberType(field.getName(), (Number) field.get(o));
                } else if (!ifObject(name) && field.getType().isInstance(new Date()))
                    pactDslJsonBody.date(field.getName(), "yyyy-MM-dd HH:mm:ss", (Date) field.get(o));
                else if (/*ifObject(name) &&*/ field.getType().newInstance() instanceof Object) {
                    PactDslJsonBody innerBodyObj = pactDslJsonBody.object(field.getName());
                    Object innerObj = field.get(o);
                    return buildJson(innerBodyObj, innerObj);
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return pactDslJsonBody;

    }

    static boolean ifObject(String fieldTypeName) {
        return fieldTypeName.equals("java.lang.Object");
    }
}
