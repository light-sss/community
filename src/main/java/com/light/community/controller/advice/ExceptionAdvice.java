package com.light.community.controller.advice;

import com.light.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author light
 * @Description 统一处理异常：记录异常日志
 *
 * @ControllerAdvice
 * - 用于修饰类，表示该类是Controller的全局配置类。
 * - 在此类中，可以对Controller进行如下三种全局配置：
 * 异常处理方案、绑定数据方案、绑定参数方案。
 * • @ExceptionHandler - 用于修饰方法，该方法会在Controller出现异常后被调用
 * 用于处理捕获到的异常
 *
 * @create 2023-05-03 21:04
 */

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
	private static final Logger logger=LoggerFactory.getLogger(ExceptionAdvice.class);

	@ExceptionHandler(Exception.class)
	public void handleException(Exception e, HttpServletResponse response, HttpServletRequest request) throws IOException {
		//当方法被调用时说明controller发生异常，将异常记录到日志中
		logger.error("服务器发生异常："+e.getMessage());
		//记录异常栈的详细信息
		for(StackTraceElement element:e.getStackTrace()){
			logger.error(element.toString());//记录每个异常的详细信息
		}

		//抛出异常后进行响应
		//判断请求为是普通请求（响应页面）还是异步请求（相应json字符串）
		String xRequestedWith = request.getHeader("x-requested-with");
		if("XMLHttpRequest".equals(xRequestedWith)){
			//异步请求:相应json字符串
			response.setContentType("application/plain;charset=utf-8"); //响应一个普通字符串。需要人为转换为json字符串
			//获取输出流输出一个字符串
			PrintWriter writer=response.getWriter();
			writer.write(CommunityUtil.getJsonString(1,"服务器异常！"));

		}else{
			//相应错误页面
			response.sendRedirect(request.getContextPath()+"/error");
		}

	}

}
