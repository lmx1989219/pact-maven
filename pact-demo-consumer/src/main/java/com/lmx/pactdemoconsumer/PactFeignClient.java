package com.lmx.pactdemoconsumer;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("sc.provider")
public interface PactFeignClient {

    @RequestMapping(value = "/api/pact",method = RequestMethod.POST)
    PactHttp.Resp hello(PactHttp.Req body);
}
