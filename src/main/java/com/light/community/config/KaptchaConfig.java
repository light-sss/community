package com.light.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author light
 * @Description  Kaptcha配置类
 * @create 2023-03-26 21:56
 */
@Configuration  //需要加注解：表示这是一个配置类
public class KaptchaConfig {
    @Bean  //相当于实现Producer接口
    public Producer kaptchaProdecer(){
        //实例化properties
        Properties properties=new Properties();
        properties.setProperty("kaptcha.image.width","100");
        properties.setProperty("kaptcha.image.height","40");
        properties.setProperty("kaptcha.textproducer.font.size","32");
        properties.setProperty("kaptcha.textproducer.font.color","0,0,0");
        properties.setProperty("kaptcha.textproducer.char.string","0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        properties.setProperty("kaptcha.textproducer.char.length","4");
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");
        //实例化实现类
        DefaultKaptcha kaptcha=new DefaultKaptcha();
        //需要传入一些配置参数 封装进config对象中：需要传入properties对象
        Config config=new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
