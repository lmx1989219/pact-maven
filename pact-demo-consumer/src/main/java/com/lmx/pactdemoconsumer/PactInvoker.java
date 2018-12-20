package com.lmx.pactdemoconsumer;

import au.com.dius.pact.consumer.ConsumerPactBuilder;
import au.com.dius.pact.consumer.PactVerificationResult;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.MockProviderConfig;
import au.com.dius.pact.model.RequestResponsePact;
import com.alibaba.fastjson.JSONObject;

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

    private static PactInvoker pactInvoker = new PactInvoker();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Cdc cdc = method.getDeclaringClass().getDeclaredAnnotation(Cdc.class);
        CdcInfo cdcInfo = method.getDeclaredAnnotation(CdcInfo.class);
        Object reqBody = args[0];
        PactDslJsonBody req = new PactDslJsonBody();
        buildJson(reqBody.getClass(), req);
        PactDslJsonBody resp = new PactDslJsonBody();
        buildJson(method.getReturnType(), resp);
        RequestResponsePact pact = ConsumerPactBuilder
                .consumer(cdc.consumer())
                .hasPactWith(cdc.provider())
                .uponReceiving(cdc.reqDesc())
                    .path(cdcInfo.reqPath())
                    .method(cdcInfo.reqMethod())
                    .body(req)
                .willRespondWith()
                    .status(200)
                    .body(resp)
                .toPact();
        MockProviderConfig config = MockProviderConfig.createDefault();
        StringBuilder stringBuilder = new StringBuilder();
        PactVerificationResult result = runConsumerTest(pact, config, mockServer -> {
            try {
                org.json.JSONObject jsonObject = new PactProviderClient(mockServer.getUrl()).pactMock((org.json.JSONObject) req.getBody(), cdcInfo.reqPath());
//                assertEquals(jsonObject.toString(),
//                        JSONObject.toJSONString(resp.getBody()));
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
     * 直接以req、resp对象属性来构建期望的请求和响应，而不是对象属性值
     * <p>
     * 定义格式规范：如日期
     * </p>
     *
     * @param cls
     * @param pactDslJsonBody
     */
    void buildJson(Class cls, PactDslJsonBody pactDslJsonBody) {
        for (Field field : cls.getDeclaredFields()) {
            if (field.getType().isInstance(new String()))
                pactDslJsonBody.stringType(field.getName());
            if (field.getType().isInstance(new Integer(1))
                    || field.getType().isInstance(new Long(1))
                    || field.getType().isInstance(new BigDecimal(1)))
                pactDslJsonBody.numberType(field.getName());
            if (field.getType().isInstance(new Date()))
                pactDslJsonBody.date(field.getName(), "yyyy-MM-dd HH:mm:ss");
        }
    }
}
