package com.light.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author light
 * @Description
 * @create 2023-04-14 23:02
 */

@Target(ElementType.METHOD)//此自定义注解在方法上生效
@Retention(RetentionPolicy.RUNTIME)//此自定义注解在运行时生效
public @interface LoginRequired {
}
