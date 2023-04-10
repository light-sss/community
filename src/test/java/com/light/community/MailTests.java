package com.light.community;

import com.light.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @author light
 * @Description  发送邮件测试类
 * @create 2023-03-25 9:56
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)//希望启用此配置类
public class MailTests {
    @Autowired
    private MailClient mailClient;

//    调用thymeleaf模板引擎：有一个核心类，被Spring管理，需要使用时直接注入
    @Autowired
    private TemplateEngine templateEngine;
    @Test
    public void testHtmlMail(){//利用模板引擎发送HTML邮件
        //访问模板需要传参：参数由Content构造
        Context context=new Context();
        context.setVariable("userName","hello-HTML");//将要传给模板变量存入content对象中
        //调用模板引擎生成动态网页
        String content = templateEngine.process("/mail/demo", context);//路径；数据
        System.out.println(content);

        //thymeleaf只是帮助生成动态网页，发邮件还是由mailClient发送
        mailClient.sendMail("2272481635@qq.com","HTML",content);
    }

    @Test
    public void testTextMail(){
        mailClient.sendMail("2272481635@qq.com","验证信息","hello-java");
    }
}
