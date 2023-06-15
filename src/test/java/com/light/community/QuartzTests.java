package com.light.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author light
 * @Description 删除任务调度器
 * @create 2023-06-15 21:23
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)//希望启用此配置类
public class QuartzTests {
	@Autowired
	private Scheduler scheduler;  //注入调度器

	@Test
	public void deleteJob(){
		try {
			boolean result = scheduler.deleteJob(new JobKey("alphaJob", "alphaJobGroup"));
			System.out.println(result);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}

}
