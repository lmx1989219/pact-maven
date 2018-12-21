package com.lmx.pactdemoconsumer;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;

public class PactDslJsonBodyUtil {
    /**
     * 以req对象来构建期望的请求
     * <p>
     * 定义格式规范：如日期
     * </p>
     *
     * @param o
     * @param pactDslJsonBody
     */
    public static PactDslJsonBody buildReqJson(PactDslJsonBody pactDslJsonBody, Object o) throws Exception {
        for (Field field : o.getClass().getDeclaredFields()) {
            if (field.getType().isInstance(new String())) {
                field.setAccessible(true);
                pactDslJsonBody.stringValue(field.getName(), (String) field.get(o));
            } else if (field.getType().isInstance(new Integer(1))
                    || field.getType().isInstance(new Long(1))
                    || field.getType().isInstance(new BigDecimal(1)))
                pactDslJsonBody.numberType(field.getName());
            else if (field.getType().isInstance(new Date()))
                pactDslJsonBody.date(field.getName(), "yyyy-MM-dd HH:mm:ss");
            else if (field.getType().newInstance() instanceof Object) {
                PactDslJsonBody innerBodyObj = pactDslJsonBody.object(field.getName());
                field.setAccessible(true);
                Object innerObj = field.get(o);
                return buildReqJson(innerBodyObj, innerObj);
            }
        }
        return pactDslJsonBody;

    }

    /**
     * 正则匹配响应值
     * <p>
     * 定义格式规范：如日期和初始值
     * </p>
     *
     * @param cls
     * @param pactDslJsonBody
     */
    public static void buildRespJson(Class cls, PactDslJsonBody pactDslJsonBody) {
        for (Field field : cls.getDeclaredFields()) {
            if (field.getType().isInstance(new String()))
                pactDslJsonBody.stringMatcher(field.getName(), "^[A-Za-z0-9]+$", "nZroXQogwHTRfpsyCZ98");
            if (field.getType().isInstance(new Integer(1))
                    || field.getType().isInstance(new Long(1))
                    || field.getType().isInstance(new BigDecimal(1)))
                pactDslJsonBody.numberType(field.getName());
            if (field.getType().isInstance(new Date()))
                pactDslJsonBody.date(field.getName(), "yyyy-MM-dd HH:mm:ss", new Date());
        }
    }
}
