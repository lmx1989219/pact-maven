package com.lmx.pactdemoconsumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 适用于restTemplate,httpClient这样没有客户端代码的情况
 */
@Cdc(provider = "Some Provider", consumer = "Some Consumer", reqDesc = "hello pact")
public interface PactHttp {
    @CdcInfo(reqPath = "/api/pact")
    Resp<LoginDto> hello(Req body);

    @CdcInfo(reqPath = "/api/pact")
    String hello(String body);

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
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class Resp<T> {
        private Integer code;
        private String message;
        private T data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class LoginDto {
        private String token;
        private String lastLoginIp;
        private Date lastLoginTime;
    }
}
