package com.lmx.pactdemoconsumer;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 契约测试执行器
 * <p>
 * 目标类：feignClient
 */
public class PactFeignClientInvoker extends AbstractPactInvoker {

    public Object getProxyObj(Class interface_) {
        return Proxy.newProxyInstance(PactFeignClientInvoker.class.getClassLoader(), new Class[]{interface_}, this);
    }

    @Override
    public PactEntity buildPact(Method method) {
        FeignClient cdc = method.getDeclaringClass().getDeclaredAnnotation(FeignClient.class);
        RequestMapping cdcInfo = method.getDeclaredAnnotation(RequestMapping.class);
        return PactEntity.builder()
                .consumer(System.getProperty("spring.application.name"))
                .provider(cdc.value())
                .upon("a api desc")
                .methodDesc(cdcInfo.method() == null ? "POST" : cdcInfo.method()[0].toString())
                .path(cdcInfo.value()[0])
                .build();
    }
}
