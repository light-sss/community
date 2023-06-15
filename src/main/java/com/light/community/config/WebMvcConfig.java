package com.light.community.config;

import com.light.community.controller.interceptor.DataInterceptor;
import com.light.community.controller.interceptor.LoginTicketInterceptor;
import com.light.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author light
 * @Description
 * @create 2023-04-05 20:57
 */

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;//将拦截器注入

    //@Autowired
    //private LoginRequiredInterceptor loginRequiredInterceptor;

    //用springsecurity替代

    @Autowired
    private MessageInterceptor messageInterceptor;


    @Autowired
    private DataInterceptor dataInterceptor;

    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.jpeg","/**/*.png");

        //registry.addInterceptor(loginRequiredInterceptor)
        //        .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.jpeg","/**/*.png");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.jpeg","/**/*.png");


        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.jpeg","/**/*.png");
    }
}
