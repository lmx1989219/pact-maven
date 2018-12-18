package com.lmx.pactdemoconsumer;

import au.com.dius.pact.consumer.ConsumerPactBuilder;
import au.com.dius.pact.consumer.PactVerificationResult;
import au.com.dius.pact.model.MockProviderConfig;
import au.com.dius.pact.model.RequestResponsePact;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static au.com.dius.pact.consumer.ConsumerPactRunnerKt.runConsumerTest;
import static org.junit.Assert.assertEquals;


public class DirectDSLConsumerPactTest {
    @Test
    public void testPact() {
        RequestResponsePact pact = ConsumerPactBuilder
                .consumer("Some Consumer")
                .hasPactWith("Some Provider")
                .uponReceiving("a request to say Hello")
                    .headers("content-type","application/json")
                    .path("/api/pact")
                    .method("POST")
                    .body("{\"name\": \"harry\"}")
                .willRespondWith()
                    .status(200)
                    .body("{\"hello\": \"harry\"}")
                .uponReceiving("book list")
                    .headers("content-type","application/json")
                    .path("/api/book/list")
                    .method("POST")
                    .body("{\"type\": \"1\"}")
                 .willRespondWith()
                    .status(200)
                    .body("[{\"author\":\"john\"}]")
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
            throw new RuntimeException(((PactVerificationResult.Error)result).getError());
        }

        assertEquals(PactVerificationResult.Ok.INSTANCE, result);
    }
}
