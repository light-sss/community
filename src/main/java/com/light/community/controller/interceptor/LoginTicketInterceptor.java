package com.light.community.controller.interceptor;

import com.light.community.entity.LoginTicket;
import com.light.community.entity.User;
import com.light.community.service.UserService;
import com.light.community.util.CookieUtil;
import com.light.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author light
 * @Description
 * @create 2023-03-31 21:37
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor{
    /*
    从请求一开始就去获取对应ticket看是否有对应的user，如果有就暂存
    （因为可能会随时随地使用到此user
     */

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;
    @Override  //在controller之前拦截
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取ticket凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        if(ticket!=null){
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //查验ticket是否有效
            if(loginTicket!=null&&loginTicket.getStatus()==0&&loginTicket.getExpired().after(new Date())) {
                //根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有用户（考虑多线程情况：将用户存入当前线程
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user!=null&&modelAndView!=null){

            modelAndView.addObject("LoginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}

/*
常用元注解
@Target：用来声明自定义的注解可以写在哪个位置，可以作用在那个类型上（方法、属性、类
@Retention:用来声明自定义注解保留的有效时间（编译时有效？运行时有效？
@Document:用来声明在生成文档时会不会将这个自定义注解也带上
@Inherited:继承，子类要不要继承父类的自定义注解

如何读取注解：通过反射：
Method.getDeclaredAnnotations()
Method.getAnnotation(Class<T> annotationClass)
 */
