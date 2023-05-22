package com.light.community.controller.interceptor;

import com.light.community.annotation.LoginRequired;
import com.light.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author light
 * @Description
 * @create 2023-04-14 23:07
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

	@Autowired
	private HostHolder hostHolder;
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if(handler instanceof HandlerMethod){ //判断要拦截的是否是方法
			HandlerMethod handlerMethod=(HandlerMethod) handler;
			Method method = handlerMethod.getMethod();//获取拦截到的方法对象
			LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);//获取该方法对象上的自定义注解
			if(loginRequired!=null&&hostHolder.getUser()==null){ //错误情况：需要被拦截
				response.sendRedirect(request.getContextPath()+"/login"); //强制重定向到登录页面
			}
		}
		return true;
	}
}
