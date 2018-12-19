package com.lmx.pactdemoconsumer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 消费驱动测试驱动类
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Cdc {
    /**
     * provider name
     * <p>
     * e.g:springCloud's appId
     *
     * @return
     */
    String provider() default "";


    String consumer() default "";

    String reqDesc() default "";
}
