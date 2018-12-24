package com.lmx.pactdemoconsumer;

import au.com.dius.pact.consumer.ConsumerPactBuilder;
import au.com.dius.pact.consumer.PactVerificationResult;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.MockProviderConfig;
import au.com.dius.pact.model.RequestResponsePact;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static au.com.dius.pact.consumer.ConsumerPactRunnerKt.runConsumerTest;
import static org.junit.Assert.assertEquals;

public abstract class AbstractPactInvoker implements InvocationHandler {
//    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        PactEntity pactEntity = buildPact(method);
        Object reqBody = args[0];
        StringBuilder stringBuilder = new StringBuilder();
        if (reqBody instanceof String) {
            PactDslJsonBody resp = new PactDslJsonBody();
            PactDslJsonBody resp_ = PactUtil.buildJson(resp, pactEntity.getMockResp());
            RequestResponsePact pact = ConsumerPactBuilder
                    .consumer(pactEntity.getConsumer())
                    .hasPactWith(pactEntity.getProvider())
                    .uponReceiving(pactEntity.getUpon())
                    .path(pactEntity.getPath())
                    .method(pactEntity.getMethodDesc())
                    .body((String) reqBody)
                    .willRespondWith()
                    .status(200)
                    .body(resp_)
                    .toPact();
            MockProviderConfig config = MockProviderConfig.createDefault();
            PactVerificationResult result = runConsumerTest(pact, config, mockServer -> {
                org.json.JSONObject jsonObject = new PactProviderClient(mockServer.getUrl()).pactMock(new org.json.JSONObject((String) reqBody), pactEntity.getPath());
                stringBuilder.append(jsonObject.toString());
            });
            if (result instanceof PactVerificationResult.Error) {
                throw new RuntimeException(((PactVerificationResult.Error) result).getError());
            }
            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        } else {
            PactDslJsonBody req = new PactDslJsonBody();
            PactDslJsonBody req_ = PactUtil.buildJson(req, reqBody);
            PactDslJsonBody resp = new PactDslJsonBody();
            PactDslJsonBody resp_ = PactUtil.buildJson(resp, pactEntity.getMockResp());
            RequestResponsePact pact = ConsumerPactBuilder
                    .consumer(pactEntity.getConsumer())
                    .hasPactWith(pactEntity.getProvider())
                    .uponReceiving(pactEntity.getUpon())
                    .path(pactEntity.getPath())
                    .method(pactEntity.getMethodDesc())
                    .body(req_)
                    .willRespondWith()
                    .status(200)
                    .body(resp_)
                    .toPact();
            MockProviderConfig config = MockProviderConfig.createDefault();
            PactVerificationResult result = runConsumerTest(pact, config, mockServer -> {
                try {
                    org.json.JSONObject jsonObject = new PactProviderClient(mockServer.getUrl()).pactMock((org.json.JSONObject) req.getBody(), pactEntity.getPath());
//                    assertEquals(jsonObject.toString(), resp);
                    stringBuilder.append(jsonObject.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            if (result instanceof PactVerificationResult.Error) {
                throw new RuntimeException(((PactVerificationResult.Error) result).getError());
            }
            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
        return JSONObject.parseObject(stringBuilder.toString(), method.getReturnType());
    }

    public abstract Object getProxyObj(Class interface_);

    public abstract PactEntity buildPact(Method method);
}
