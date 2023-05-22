package com.light.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author light
 * @Description
 * @create 2023-05-03 21:51
 */

//@Component
//@Aspect
public class AlphaAspect {

	//声明切点
	//* com.light.community.service.*.*(..):所有的返回值  service下所有组件、所有方法、方法中所有参数、所有返回值
	@Pointcut("execution(* com.light.community.service.*.*(..))")
	public void pointcut(){

	}

	//定义通知：利用通知明确解决问题
	@Before("pointcut()") //连接点一开始执行通知
	public void before(){
		System.out.println("before........");
	}

	@After("pointcut()")//连接点之后执行通知
	public void after(){
		System.out.println("after......");
	}

	@AfterReturning("pointcut()") //在返回值之后执行
	public void afterReturning(){
		System.out.println("afterReturning......");
	}

	@AfterThrowing("pointcut()") //在抛异常之后之后执行
	public void afterThrowing(){
		System.out.println("afterThrowing......");
	}

	@Around("pointcut()") //在切点前后都之后执行
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
		System.out.println("aroundBefore......");
		Object obj = joinPoint.proceed();//调用目标组件方法（调用目标对象被处理的方法
		System.out.println("aroundAfter......");
		return obj;
	}
}
