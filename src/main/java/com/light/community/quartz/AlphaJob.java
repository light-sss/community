package com.light.community.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 *  * Quartz核心组件
 *  * 	 *
 *  * 	 * 1.scheduler:Quartz的核心调度工具，所有的任务都是通过该接口去调用的
 *  * 	 *
 *  * 	 * 首先需要定义任务（job
 *  * 	 * 配置jobDetail（配置job参数
 *  * 	 * 配置trigger(配置job什么时候运行
 *  * 	 *
 *  * 	 * Quartz根据配置好的信息来读取任务，将这些信息都读取到数据库表中，之后通过读取数据库表来执行任务
 *  * 	 *
 */

/**
 * @author light
 * @Description  定义一个任务
 *
 * @create 2023-06-15 20:42
 */
public class AlphaJob implements Job {
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		System.out.println(Thread.currentThread().getName()+": execute a quartz job.");
	}
}
