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
                .headers("content-type", cdcInfo.reqHead())
                .path(cdcInfo.reqPath())
                .method(cdcInfo.reqMethod())
                .body(req)
                .willRespondWith()
                .status(200)
                .body(resp)
                .toPact();
        MockProviderConfig config = MockProviderConfig.createDefault();
        PactVerificationResult result = runConsumerTest(pact, config, mockServer -> {
            try {
                assertEquals(JSONObject.toJSONString(new PactProviderClient(mockServer.getUrl()).pactMock((org.json.JSONObject) req.getBody(), cdcInfo.reqPath())),
                        JSONObject.toJSONString(resp.getBody()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        if (result instanceof PactVerificationResult.Error) {
            throw new RuntimeException(((PactVerificationResult.Error) result).getError());
        }
        assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        return null;
    }

    public static Object getProxyObj(Class interface_) {
        return Proxy.newProxyInstance(PactInvoker.class.getClassLoader(), new Class[]{interface_}, pactInvoker);
    }

    void buildJson(Class cls, PactDslJsonBody pactDslJsonBody) {
        for (Field field : cls.getDeclaredFields()) {
            if (field.getType().isInstance(new String()))
                pactDslJsonBody.stringType(field.getName());
            if (field.getType().isInstance(new Integer(1))
                    || field.getType().isInstance(new Long(1)))
                pactDslJsonBody.numberType(field.getName());
            if (field.getType().isInstance(new Date()))
                pactDslJsonBody.date(field.getName(), "yyyy-MM-dd HH:mm:ss");
        }
    }
}
