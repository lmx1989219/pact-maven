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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static au.com.dius.pact.consumer.ConsumerPactRunnerKt.runConsumerTest;
import static org.junit.Assert.assertEquals;

public abstract class AbstractPactInvoker implements InvocationHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        PactEntity pactEntity = buildPact(method);
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
                .consumer(pactEntity.getConsumer())
                .hasPactWith(pactEntity.getProvider())
                .uponReceiving(pactEntity.getUpon())
                .path(pactEntity.getPath())
                .method(pactEntity.getMethodDesc())
                .body(req_)
                .willRespondWith()
                .status(200)
                .body(resp)
                .toPact();
        MockProviderConfig config = MockProviderConfig.createDefault();
        StringBuilder stringBuilder = new StringBuilder();
        PactVerificationResult result = runConsumerTest(pact, config, mockServer -> {
            try {
                org.json.JSONObject jsonObject = new PactProviderClient(mockServer.getUrl()).pactMock((org.json.JSONObject) req.getBody(), pactEntity.getPath());
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

    public abstract Object getProxyObj(Class interface_);

    public abstract PactEntity buildPact(Method method);
}
