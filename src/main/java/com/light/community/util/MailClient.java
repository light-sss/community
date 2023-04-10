package com.light.community.util;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.swing.text.html.HTML;


/**
 * @author light
 * @Description
 * @create 2023-03-24 17:06
 */
@Component
public class MailClient {

    //记录日志
    private static final Logger logger= LoggerFactory.getLogger(MailClient.class);
    //将发送邮件功能注入
    @Autowired
    private JavaMailSender mailSender;

    //发送邮件发送人是固定的，因此直接将发送人注入到bean中
    @Value("${spring.mail.username}")
    private String from;//发件人




    /**
     * 封装一个公有方法能够被外界调用（只要不报错，邮件就发送成功
     * @param to  发送目标
     * @param subject  发送邮件主题
     * @param content  发送内容
     */
    public void sendMail(String to,String subject,String content){
        try {
            MimeMessage message=mailSender.createMimeMessage();
            //使用帮助类帮助创建message更详细内容
            MimeMessageHelper helper=new MimeMessageHelper(message);
            helper.setFrom(from);//设置发件人
            helper.setTo(to);//设置收件人
            helper.setSubject(subject);//设置发送主题
            helper.setText(content, true);//设置发送内容：不加true，默认普通文本，加了参数表示支持HTML文本
            mailSender.send(helper.getMimeMessage());//发送邮件
        } catch (MessagingException e) {
            logger.error("发送邮件失败："+e.getMessage());
        }

    }
}
