package com.light.community;

import com.light.community.dao.AlphaDao;
import com.light.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)//希望启用此配置类
class CommunityApplicationTests implements ApplicationContextAware {

	//哪个类想得到容器哪个类就实现ApplicationContextAware接口

	private ApplicationContext applicationContext;
	@Test
	void contextLoads() {
	}


	//容器会监测到哪个类实现了ApplicationContextAware接口的setApplicationContext()方法，然后会将自身传入
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext=applicationContext;//记录容器
	}

	@Test
	public void testApplication(){
		System.out.println(applicationContext);//测试容器

		//从容器中获取自动装配的bean
		AlphaDao alphaDao=applicationContext.getBean(AlphaDao.class);

		System.out.println(alphaDao.select());

	}

	@Test
	public void testBeanManagement(){
		/*
		测试bean的管理方式
		 */

		//通过容器获取Service
		AlphaService alphaService=applicationContext.getBean(AlphaService.class);
		System.out.println(alphaService);
		/*
		程序只被实例化一次，只被销毁一次，是单例的
		被spring管理的bean，是单例的
		 */
	}

	@Test
	public void testBeanConfig(){
		SimpleDateFormat simpleDateFormat=applicationContext.getBean(SimpleDateFormat.class);
		System.out.println(simpleDateFormat.format(new Date()));
	}

	@Autowired //声明要给当前bean注入给alphaDao
	private AlphaDao alphaDao;//声明成员变量

	@Test
	public void testDI(){
		//依赖注入DI
		System.out.println(alphaDao);
	}

	/*
	项目综合：在开发中由Controller处理浏览器请求，遭处理过程中会调用业务组件处理当前业务，业务组件会调用Dao去访问数据库
			（即：Controller调用Service；Service调用Dao）彼此是互相依赖的，就可用依赖注入的方式去实现
	 */


}
