package com.light.community.config;

import com.light.community.quartz.AlphaJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @author light
 * @Description  Quartz配置【jobDetail、trigger】:在第一次初始化信息时，将信息存入数据库中
 * @create 2023-06-15 20:44
 */

@Configuration
public class QuartzConfig {
	/**
	 * FactoryBean和BeanFactory
	 *
	 * BeanFactory:IOC容器顶层接口
	 *
	 *
	 * FactoryBean：可简化bean的实例化过程
	 * 	1.通过FactoryBean封装了某些bean的实例化过程
	 * 	2.将FactoryBean装配到spring中
	 * 	3.将FactoryBean注入给其他的bean
	 * 	4.该bean得到的是FactoryBean所管理的对象实例
	 *
	 */

	//配置jobDetail
	//@Bean
	public JobDetailFactoryBean alphaJobDetail(){
		JobDetailFactoryBean factoryBean=new JobDetailFactoryBean(); //实例化对象
		//设置属性
		factoryBean.setJobClass(AlphaJob.class);
		factoryBean.setName("alphaJob");
		factoryBean.setGroup("alphaJobGroup");
		factoryBean.setDurability(true);  //该任务是否长久保存
		factoryBean.setRequestsRecovery(true);  //该任务是否可恢复的
		return factoryBean;
	}

	//配置trigger（依赖于jobDetail，需将jobDetail注入）【SimpleTriggerFactoryBean（简单trigger），CronTriggerFactoryBean(复杂的trigger)】
	//@Bean
	public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
		SimpleTriggerFactoryBean factoryBean=new SimpleTriggerFactoryBean();
		factoryBean.setJobDetail(alphaJobDetail);
		factoryBean.setName("alphaTrigger");
		factoryBean.setGroup("alphaTriggerGroup");
		factoryBean.setRepeatInterval(3000); //时间间隔
		factoryBean.setJobDataMap(new JobDataMap()); //trigger底层需要存储job的一些状态（初始化了一个默认的存储方式
		return factoryBean;
	}

}
