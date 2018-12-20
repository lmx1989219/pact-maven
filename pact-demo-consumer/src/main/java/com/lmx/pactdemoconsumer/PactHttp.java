package com.lmx.pactdemoconsumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 模拟feignClient
 */
@Cdc(provider = "Some Provider", consumer = "Some Consumer", reqDesc = "hello pact")
public interface PactHttp {
    @CdcInfo(reqPath = "/api/pact")
    Resp hello(Req body);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class Req {
        private String name;
        private String pwd;
        private Long expire;
        private Date loginTime;
        private InnerReq inner;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class InnerReq {
        private String tel;
        private String email;
    }

    @Data
    @AllArgsConstructor
    class Resp {
        private String token;
        private String lastLoginIp;
        private Date lastLoginTime;
    }
}
