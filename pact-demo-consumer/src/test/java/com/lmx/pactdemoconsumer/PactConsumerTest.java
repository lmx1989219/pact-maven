package com.lmx.pactdemoconsumer;

import au.com.dius.pact.consumer.ConsumerPactBuilder;
import au.com.dius.pact.consumer.PactVerificationResult;
import au.com.dius.pact.model.MockProviderConfig;
import au.com.dius.pact.model.RequestResponsePact;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static au.com.dius.pact.consumer.ConsumerPactRunnerKt.runConsumerTest;
import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonArray;
import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.Assert.assertEquals;

@Slf4j
public class PactConsumerTest {
    PactInvoker pactInvoker = new PactInvoker();
    PactFeignClientInvoker pactFeignClientInvoker = new PactFeignClientInvoker();

    /**
     * simple demo
     */
    @Test
    public void testPact() {
        RequestResponsePact pact = ConsumerPactBuilder
                .consumer("Some Consumer")
                .hasPactWith("Some Provider")
                .given("pact-test")
                .uponReceiving("hello pact")
                .headers("content-type", "application/json")
                .path("/api/pact")
                .method("POST")
//                    .body("{\"name\": \"harry\"}")
                .body(newJsonBody((o) -> o.stringValue("name", "harry")).build())
                .willRespondWith()
                .status(200)
                .body("{\"hello\": \"harry\"}")
                .uponReceiving("book list")
                .headers("content-type", "application/json")
                .path("/api/book/list")
                .method("POST")
                .body("{\"type\": \"1\"}")
                .willRespondWith()
                .status(200)
//                    .body("[{\"author\":\"john\"}]")
                .body(newJsonArray((rootArray) -> rootArray.object((o) -> o.stringValue("author", "john"))).build())
                .toPact();

        MockProviderConfig config = MockProviderConfig.createDefault();
        PactVerificationResult result = runConsumerTest(pact, config, mockServer -> {
            try {
                Map expectedResponse = new HashMap();
                expectedResponse.put("hello", "harry");
                assertEquals(new ProviderClient(mockServer.getUrl()).hello("{\"name\": \"harry\"}"),
                        expectedResponse);
                List<Map> expectObj = new ArrayList<>();
                Map expectedResponse_ = new HashMap();
                expectedResponse_.put("author", "john");
                expectObj.add(expectedResponse_);
                assertEquals(new ProviderClient(mockServer.getUrl()).book("{\"type\": \"1\"}"),
                        expectObj);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        if (result instanceof PactVerificationResult.Error) {
            throw new RuntimeException(((PactVerificationResult.Error) result).getError());
        }

        assertEquals(PactVerificationResult.Ok.INSTANCE, result);
    }

    /**
     * support restTemplate/httpClient pact testing
     */
    @Test
    public void testProxyPact() {
        PactHttp pactHttp = (PactHttp) pactInvoker.getProxyObj(PactHttp.class);
        PactHttp.Resp resp = pactHttp.hello(new PactHttp.Req("james", "123", 100L, new Date(),
                new PactHttp.InnerReq("15821303235", "285980382@qq.com")));
        log.info("cdc resp={}", resp);
    }

    /**
     * support feignClient pact testing
     */
    @Test
    public void testProxyFeignPact() {
        PactFeignClient feignClient = (PactFeignClient) pactFeignClientInvoker.getProxyObj(PactFeignClient.class);
        PactHttp.Resp resp = feignClient.hello(new PactHttp.Req("james", "123", 100L, new Date(),
                new PactHttp.InnerReq("15821303235", "285980382@qq.com")));
        log.info("cdc resp={}", resp);
    }
}
