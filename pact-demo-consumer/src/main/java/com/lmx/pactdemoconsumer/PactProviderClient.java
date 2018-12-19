package com.lmx.pactdemoconsumer;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.Map;

public class PactProviderClient {

    private final String url;

    public PactProviderClient(String url) {
        this.url = url;
    }

    public org.json.JSONObject pactMock(org.json.JSONObject body, String path) throws IOException {
        String response = Request.Post(url + path)
                .bodyString(body.toString(), ContentType.APPLICATION_JSON)
                .execute().returnContent().asString();
        org.json.JSONObject resp = new org.json.JSONObject();
        JSONObject json = JSONObject.parseObject(response);
        for (Map.Entry e : json.entrySet())
            resp.put(e.getKey().toString(),e.getValue());
        return resp;
    }
}
