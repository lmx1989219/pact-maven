package com.lmx.pactdemoconsumer;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 契约测试执行器
 * <p>
 * 目标类：模拟restTemplate,httpClient的请求响应
 */
public class PactInvoker extends AbstractPactInvoker {

    public Object getProxyObj(Class interface_) {
        return Proxy.newProxyInstance(PactInvoker.class.getClassLoader(), new Class[]{interface_}, this);
    }

    @Override
    public PactEntity buildPact(Method method) {
        Cdc cdc = method.getDeclaringClass().getDeclaredAnnotation(Cdc.class);
        CdcInfo cdcInfo = method.getDeclaredAnnotation(CdcInfo.class);
        return PactEntity.builder()
                .consumer(cdc.consumer())
                .provider(cdc.consumer())
                .upon(cdc.reqDesc())
                .methodDesc(cdcInfo.reqMethod())
                .path(cdcInfo.reqPath())
                .build();
    }
}
