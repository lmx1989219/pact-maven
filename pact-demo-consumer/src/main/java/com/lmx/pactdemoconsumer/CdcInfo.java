package com.lmx.pactdemoconsumer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CdcInfo {

    String reqPath() default "";

    String reqMethod() default "post";

    String reqHead() default "application/json";

    String mockResp() default "{\"code\":1,\"message\":\"ok\",\"data\":null}";
}
