package com.lmx.pactdemoconsumer;

import au.com.dius.pact.consumer.ConsumerPactBuilder;
import au.com.dius.pact.consumer.PactVerificationResult;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.MockProviderConfig;
import au.com.dius.pact.model.RequestResponsePact;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static au.com.dius.pact.consumer.ConsumerPactRunnerKt.runConsumerTest;
import static org.junit.Assert.assertEquals;

/**
 * 契约测试执行器
 * <p>
 * 目标类：feignClient
 */
public class PactFeignClientInvoker implements InvocationHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static PactFeignClientInvoker pactInvoker = new PactFeignClientInvoker();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        FeignClient cdc = method.getDeclaringClass().getDeclaredAnnotation(FeignClient.class);
        RequestMapping cdcInfo = method.getDeclaredAnnotation(RequestMapping.class);
        Object reqBody = args[0];
        PactDslJsonBody req = new PactDslJsonBody();
        PactDslJsonBody req_ = null;
        try {
            req_ = PactDslJsonBodyUtil.buildReqJson(req, reqBody);
        } catch (Exception e) {
            logger.error("", e);
        }
        PactDslJsonBody resp = new PactDslJsonBody();
        PactDslJsonBodyUtil.buildRespJson(method.getReturnType(), resp);
        RequestResponsePact pact = ConsumerPactBuilder
                .consumer(System.getProperty("spring.application.name"))
                .hasPactWith(cdc.value())
                .uponReceiving("it's a feign api")
                .path(cdcInfo.value()[0])
                .method(cdcInfo.method() == null ? "POST" : cdcInfo.method()[0].toString())
                .body(req_)
                .willRespondWith()
                .status(200)
                .body(resp)
                .toPact();
        MockProviderConfig config = MockProviderConfig.createDefault();
        StringBuilder stringBuilder = new StringBuilder();
        PactVerificationResult result = runConsumerTest(pact, config, mockServer -> {
            try {
                org.json.JSONObject jsonObject = new PactProviderClient(mockServer.getUrl()).pactMock((org.json.JSONObject) req.getBody(), cdcInfo.value()[0]);
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
        return Proxy.newProxyInstance(PactFeignClientInvoker.class.getClassLoader(), new Class[]{interface_}, pactInvoker);
    }
}
