package com.doak.common.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ：zhanyiqun
 * @date ：Created in 2020/9/13 12:05
 * @description：表示这个是SPI机制的扩展类
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {

    /**
     * 表示默认的spi的扩展类实现对应的key
     */
    String value() default "";
}
