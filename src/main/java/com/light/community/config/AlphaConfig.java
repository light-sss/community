package com.light.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

/**
 * @author light
 * @Description
 * @create 2023-03-21 19:19
 */
//配置类实例，装载第三方jar包
@Configuration
public class AlphaConfig {
    @Bean   //装配第三方Bean
    public SimpleDateFormat simpleDateFormat(){
        //这个方法返回的对象将被装配到容器中
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
