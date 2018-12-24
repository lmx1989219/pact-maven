package com.lmx.pactdemoconsumer;

import au.com.dius.pact.consumer.ConsumerPactBuilder;
import au.com.dius.pact.consumer.PactVerificationResult;
import au.com.dius.pact.model.MockProviderConfig;
import au.com.dius.pact.model.RequestResponsePact;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
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
//    @Test
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
     * mock responseEntity
     */
    @Before
    public void bindMockResp() {
        PactHttp.Resp respMock = PactHttp.Resp.builder().code(1).message("ok").data(
                PactHttp.LoginDto.builder()
                        .lastLoginIp("127.0.0.1")
                        .lastLoginTime(new Date())
                        .token("1234567890ABCDEF")
                        .build()
        ).build();
        PactUtil.pactHolder.set(respMock);
    }

    /**
     * support restTemplate/httpClient pact testing
     */
    @Test
    public void testMVCPactObj() {
        PactHttp pactHttp = (PactHttp) pactInvoker.getProxyObj(PactHttp.class);
        PactHttp.Resp resp = pactHttp.hello(new PactHttp.Req("james", "123", 100L, new Date(),
                new PactHttp.InnerReq("15821303235", "285980382@qq.com")));
        log.info("cdc resp={}", resp);
    }

    @Test
    public void testMVCPactStr() {
        PactHttp pactHttp = (PactHttp) pactInvoker.getProxyObj(PactHttp.class);
        String resp = pactHttp.hello("{\n" +
                "                    \"loginTime\": \"2018-12-24 14:54:35\",\n" +
                "                    \"expire\": 100,\n" +
                "                    \"name\": \"james\",\n" +
                "                    \"pwd\": \"123\",\n" +
                "                    \"inner\": {\n" +
                "                        \"tel\": \"15821303235\",\n" +
                "                        \"email\": \"285980382@qq.com\"\n" +
                "                    }\n" +
                "                }");
        log.info("cdc resp={}", resp);
    }

    /**
     * support feignClient pact testing
     */
    @Test
    public void testFeignPact() {
        PactFeignClient feignClient = (PactFeignClient) pactFeignClientInvoker.getProxyObj(PactFeignClient.class);
        PactHttp.Resp resp_ = feignClient.hello(new PactHttp.Req("james", "123", 100L, new Date(),
                new PactHttp.InnerReq("15821303235", "285980382@qq.com")));
        log.info("cdc resp={}", resp_);
    }
}
