package com.light.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;

/**
 * @author light
 * @Description
 * @create 2023-05-04 19:35
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)//希望启用此配置类
public class RedisTests {
	@Autowired
	private RedisTemplate redisTemplate;

	@Test
	public void testStrings(){
		String redisKey="test:count";
		redisTemplate.opsForValue().set(redisKey,1);
		System.out.println(redisTemplate.opsForValue().get(redisKey));
	}

	//多次访问同一个key
	@Test
	public void testBoundOperations(){
		String redisKey="test:count";
		BoundValueOperations operations=redisTemplate.boundValueOps(redisKey);
		System.out.println(operations.get());
	}

	//编程式事务
	//在redis事务中不要做查询
	@Test
	public void testTransactional(){
		Object obj = redisTemplate.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations operations) throws DataAccessException {
				String redisKey="test:tx";
				operations.multi();
				operations.opsForSet().add(redisKey,"zhangsan");
				operations.opsForSet().add(redisKey,"lisi");
				operations.opsForSet().add(redisKey,"wangwu");
				System.out.println(operations.opsForSet().members(redisKey));
				return operations.exec();
			}
		});
		System.out.println(obj);
	}
}
