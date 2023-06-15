package com.light.community;

import com.light.community.service.AlphaService;
import javafx.concurrent.ScheduledService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * @author light
 * @Description
 * @create 2023-06-15 17:56
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)//希望启用此配置类
public class ThreadPoolTests {
	public static final Logger logger= LoggerFactory.getLogger(ThreadPoolTests.class);

	//jdk普通线程池
	private ExecutorService executorService= Executors.newFixedThreadPool(5);

	//jdk可执行定时任务的线程池
	private ScheduledExecutorService scheduledExecutorService=Executors.newScheduledThreadPool(5);

	//spring普通线程池
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	//spring可执行定时任务线程池
	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;


	@Autowired
	private AlphaService alphaService;


	private void sleep(long m){ //m:毫秒
		try {
			Thread.sleep(m);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	//1.jdk普通线程池测试
	@Test
	public void testExecutorService(){
		Runnable task = new Runnable() {   //定义任务
			@Override
			public void run() {
				logger.debug("hello ExecutorService");
			}
		};

		for (int i = 0; i < 10; i++) {
			executorService.submit(task);
		}
		sleep(10000); //10秒
	}

	//2.jdk可执行定时任务的线程池（设置时间间隔定时执行任务
	@Test
	public void testScheduledExecutorService(){
		Runnable task = new Runnable() {  //定义任务
			@Override
			public void run() {
				logger.debug("hello ScheduledExecutorService");
			}
		};

		/**
		 * scheduledExecutorService.scheduleAtFixedRate(任务,任务延迟所少毫秒才执行,反复执行的时间间隔,单位【秒/毫秒】)
		 */
		scheduledExecutorService.scheduleAtFixedRate(task,10000,1000, TimeUnit.MILLISECONDS); //以固定频率执行

		sleep(30000);
	}

	//3.spring普通线程池
	@Test
	public void testThreadPoolTaskExecutor(){
		Runnable task = new Runnable() {  //定义任务
			@Override
			public void run() {
				logger.debug("hello ThreadPoolTaskExecutor");
			}
		};

		for (int i = 0; i < 10; i++) {
			taskExecutor.submit(task);
		}
		sleep(10000);
	}

	//4.spring可执行定时任务的线程池
	@Test
	public void testThreadPoolTaskScheduler(){
		Runnable task = new Runnable() {  //定义任务
			@Override
			public void run() {
				logger.debug("hello ThreadPoolTaskExecutor");
			}
		};
		/**
		 * taskScheduler.scheduleAtFixedRate(任务,以具体的一个时间执行,时间间隔[默认毫秒，可以不用声明单位])
		 */
		Date date=new Date(System.currentTimeMillis()+10000);
		taskScheduler.scheduleAtFixedRate(task,date,1000);
		sleep(30000);
	}

	/**
	 * spring普通线程池简便方法
	 * 只需要在任意的bean中声明的方法上加上一个注解就可以执行，或直接将声明的方法体作为一个线程体
	 */

	@Test
	public void testThreadPoolTaskExecutorSimple1(){
		for (int i = 0; i < 10; i++) {

			alphaService.execute1();
		}
		sleep(10000);
	}

	/**
	 * spring定时任务线程池简便方法
	 * 只需要在任意的bean中声明的方法上加上一个注解就可以执行，或直接将声明的方法体作为一个线程体
	 */
	@Test
	public void testThreadPoolTaskExecutorSimple2(){
		sleep(30000);
	}


}
