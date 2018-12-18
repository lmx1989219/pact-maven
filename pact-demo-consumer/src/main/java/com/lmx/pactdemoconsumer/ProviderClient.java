package com.lmx.pactdemoconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProviderClient {

    private final String url;

    public ProviderClient(String url) {
        this.url = url;
    }

    public Map hello(String body) throws IOException {
        String response = Request.Post(url + "/api/pact")
                .bodyString(body, ContentType.APPLICATION_JSON)
                .execute().returnContent().asString();
        return new ObjectMapper().readValue(response, HashMap.class);
    }

    public List book(String body) throws IOException {
        String response = Request.Post(url + "/api/book/list")
                .bodyString(body, ContentType.APPLICATION_JSON)
                .execute().returnContent().asString();
        return new ObjectMapper().readValue(response, List.class);
    }
}
