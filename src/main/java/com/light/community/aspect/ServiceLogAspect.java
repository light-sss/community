package com.light.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author light
 * @Description
 * @create 2023-05-03 22:36
 */


@Component
@Aspect
public class ServiceLogAspect {
	private static final Logger logger= LoggerFactory.getLogger(ServiceLogAspect.class);

	@Pointcut("execution(* com.light.community.service.*.*(..))")
	public void pointcut(){

	}


	@Before("pointcut()")
	public void before(JoinPoint joinPoint){
		//joinPoint:指当前调用的方法
		//用户[1.2.3.4](ip地址）,在[xxx(时间)],访问了[com.light,community.service.xxx()](service组件的某个方法）
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if(attributes==null){
			return;
		}
		HttpServletRequest request = attributes.getRequest();
		String ip = request.getRemoteHost(); //获取对象的ip地址
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()); //获取时间

		String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

		logger.info(String.format("用户[%s],在[%s],访问了[%s]",ip,now,target));
	}
}
