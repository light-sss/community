package com.light.community.controller;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.light.community.entity.User;
import com.light.community.service.UserService;
import com.light.community.util.CommunityConstant;
import com.light.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;


/**
 * @author light
 * @Description 开发注册、登录功能
 * @create 2023-03-25 10:36
 */
@Controller
public class LoginController implements CommunityConstant {
    //处理访问注册请求
    @RequestMapping(value = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){

        return "/site/register";
    }
    //访问登录页面

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String getLoginPage(){

        return "/site/login";
    }

    //注入依赖：处理注册账号请求需要调用业务层
    @Autowired
    private UserService userService;//将UserService注入

    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){  //处理请求：浏览器向我们提交数据：POST请求
        Map<String, Object> map = userService.register(user);
        if(map==null||map.isEmpty()){
            //注册成功
            //提示注册成功信息---->跳转首页--->激活账号--->登录
            model.addAttribute("msg","注册成功，我们已向您邮箱发送了一封激活邮件，尽快激活账户");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("userNameMessage",map.get("userNameMessage"));
            model.addAttribute("userPasswordMessage",map.get("userPasswordMessage"));
            model.addAttribute("userEmailMessage",map.get("userEmailMessage"));
            return "/site/register";
        }
    }
    //http://localhost:8080/community/activation/101/code
    @RequestMapping(path="/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code")String code){
        int result = userService.activation(userId, code);
        if(result==ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功！");
            model.addAttribute("target","/login");

        } else if (result==ACTIVATION_REPEAT) {
            model.addAttribute("msg","无效操作，请勿重复激活！");
            model.addAttribute("target","/index");

        }else{
            model.addAttribute("msg","激活失败，请验证激活码！");
            model.addAttribute("target","/index");


        }
        return "/site/operate-result";
    }

    public static final Logger logger=  LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private Producer defaultKaptcha;

    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码（需注入bean
        String text = defaultKaptcha.createText();
        BufferedImage image = defaultKaptcha.createImage(text);
        //将验证码存入session
        session.setAttribute("kaptcha",text);
        //将图片输出给浏览器
        response.setContentType("image/png");

        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);

        } catch (IOException e) {

           logger.error("响应验证码失败："+e.getMessage());
        }

    }
    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     *
     * @param userName 用户名
     * @param password 密码
     * @param code  验证码
     * @param model 封装返回的数据
     * @param rememberMe 是否勾选记住我
     * @param session 从session中取出生成的验证码
     * @param response 如果登陆成功，将用户登录状态ticket存入客户端（cookie
     */
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String userName,String password,String code,
                     boolean rememberMe ,Model model,HttpSession session,HttpServletResponse response){
        //先判断验证码是否正确
        String kaptcha= (String) session.getAttribute("kaptcha");//得到验证码
        //得到的验证码和用户传入的验证码进行相比
        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||(!kaptcha.equalsIgnoreCase(code))){

            //验证码错误返回提示，将提示封装进model中
            model.addAttribute("CodeMsg","验证码错误！");
            return "/site/login";//回到登录页面
        }
        //验证账号、密码
        //传入过期时间：针对是否勾选"记住我"选项，有不同的过期时间
        int  expiredSeconds=rememberMe?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;

        Map<String, Object> map = userService.login(userName, password, expiredSeconds);
        if(map.containsKey("ticket")){
            //验证成功：将ticket存入cookie中
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);//设置cookie有效路径
            cookie.setMaxAge(expiredSeconds);//设置cookie有效时间
            response.addCookie(cookie);//将cookie发送给浏览器（将cookie存入到response头部
            return "redirect:/index";  //跳转至首页
        }else{
            //验证失败
            model.addAttribute("userNameMsg",map.get("userNameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";//回到登录页面
        }
    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){//从浏览器中得到cookie
        userService.logout(ticket);
        return "redirect:/login";
    }

}
