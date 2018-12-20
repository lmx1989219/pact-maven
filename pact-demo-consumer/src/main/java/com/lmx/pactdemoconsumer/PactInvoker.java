package com.lmx.pactdemoconsumer;

import au.com.dius.pact.consumer.ConsumerPactBuilder;
import au.com.dius.pact.consumer.PactVerificationResult;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.MockProviderConfig;
import au.com.dius.pact.model.RequestResponsePact;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Date;

import static au.com.dius.pact.consumer.ConsumerPactRunnerKt.runConsumerTest;
import static org.junit.Assert.assertEquals;

/**
 * 契约测试执行器
 * <p>
 * 目标类：feignClient,用于模拟暂时自己定义驱动注解
 */
public class PactInvoker implements InvocationHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static PactInvoker pactInvoker = new PactInvoker();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Cdc cdc = method.getDeclaringClass().getDeclaredAnnotation(Cdc.class);
        CdcInfo cdcInfo = method.getDeclaredAnnotation(CdcInfo.class);
        Object reqBody = args[0];
        PactDslJsonBody req = new PactDslJsonBody();
        PactDslJsonBody req_ = null;
        try {
            req_ = buildReqJson(req, reqBody);
        } catch (Exception e) {
            logger.error("", e);
        }
        PactDslJsonBody resp = new PactDslJsonBody();
        buildRespJson(method.getReturnType(), resp);
        RequestResponsePact pact = ConsumerPactBuilder
                .consumer(cdc.consumer())
                .hasPactWith(cdc.provider())
                .uponReceiving(cdc.reqDesc())
                .path(cdcInfo.reqPath())
                .method(cdcInfo.reqMethod())
                .body(req_)
                .willRespondWith()
                .status(200)
                .body(resp)
                .toPact();
        MockProviderConfig config = MockProviderConfig.createDefault();
        StringBuilder stringBuilder = new StringBuilder();
        PactVerificationResult result = runConsumerTest(pact, config, mockServer -> {
            try {
                org.json.JSONObject jsonObject = new PactProviderClient(mockServer.getUrl()).pactMock((org.json.JSONObject) req.getBody(), cdcInfo.reqPath());
                assertEquals(jsonObject.toString(), resp.getBody().toString());
                stringBuilder.append(jsonObject.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        if (result instanceof PactVerificationResult.Error) {
            throw new RuntimeException(((PactVerificationResult.Error) result).getError());
        }
        assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        return JSONObject.parseObject(stringBuilder.toString(), method.getReturnType());
    }

    public static Object getProxyObj(Class interface_) {
        return Proxy.newProxyInstance(PactInvoker.class.getClassLoader(), new Class[]{interface_}, pactInvoker);
    }

    /**
     * 以req对象来构建期望的请求
     * <p>
     * 定义格式规范：如日期
     * </p>
     *
     * @param o
     * @param pactDslJsonBody
     */
    PactDslJsonBody buildReqJson(PactDslJsonBody pactDslJsonBody, Object o) throws Exception {
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
    void buildRespJson(Class cls, PactDslJsonBody pactDslJsonBody) {
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
