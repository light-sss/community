package com.light.community;

import com.sun.org.apache.bcel.internal.generic.NEW;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;

import java.util.Random;

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

	//统计2万数据的独立总数
	@Test
	public void testHyperLogLog(){
		String redisKey="test:hll:00";
		for(int i=1;i<=10000;i++){
			redisTemplate.opsForHyperLogLog().add(redisKey,i);
		}
		for(int i=1;i<=10000;i++){
			int random = (int) (Math.random() * 100000 + 1);
			redisTemplate.opsForHyperLogLog().add(redisKey,random);
		}
		Long size = redisTemplate.opsForHyperLogLog().size(redisKey);
		System.out.println(size);
	}

	//将3组数据合并，再求出总的独立访问总数
	@Test
	public void testHyperLogLogUnion(){
		String redisKey1="test:hll:01";
		for(int i=1;i<=1000;i++){
			redisTemplate.opsForHyperLogLog().add(redisKey1,i);
		}
		String redisKey2="test:hll:02";
		for(int i=500;i<=1500;i++){
			redisTemplate.opsForHyperLogLog().add(redisKey2,i);
		}
		String redisKey3="test:hll:03";
		for(int i=1001;i<=2000;i++){
			redisTemplate.opsForHyperLogLog().add(redisKey3,i);
		}

		String union="test:hll:union";
		redisTemplate.opsForHyperLogLog().union(union,redisKey1,redisKey2,redisKey3);

		Long size = redisTemplate.opsForHyperLogLog().size(union);
		System.out.println(size);
	}

	//bitmap
	//统计一组数据布尔值
	@Test
	public void testBitMap(){
		String redisKey="test:bm:00";

		//记录
		redisTemplate.opsForValue().setBit(redisKey,1,true);  //设置某一位的值
		redisTemplate.opsForValue().setBit(redisKey,4,true);
		redisTemplate.opsForValue().setBit(redisKey,7,true);

		//查询
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));  //获取某一位的值
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

		//统计一共有多少个数（为true
		//redisTemplate.execute():执行一个redis命令，需要传入一个回调的接口，该接口中获得一个redis连接
		Object obj = redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
				return redisConnection.bitCount(redisKey.getBytes());  //为1的个数--->true
			}
		});

		System.out.println(obj);
	}

	//统计3组数据的布尔值,并对这三组数据做or运算
	@Test
	public void testBitMapOperation(){
		String redisKey1="test:bm:01";

		//记录
		redisTemplate.opsForValue().setBit(redisKey1,0,true);  //设置某一位的值
		redisTemplate.opsForValue().setBit(redisKey1,1,true);
		redisTemplate.opsForValue().setBit(redisKey1,2,true);

		String redisKey2="test:bm:02";

		//记录
		redisTemplate.opsForValue().setBit(redisKey2,2,true);  //设置某一位的值
		redisTemplate.opsForValue().setBit(redisKey2,3,true);
		redisTemplate.opsForValue().setBit(redisKey2,4,true);

		String redisKey3="test:bm:03";

		//记录
		redisTemplate.opsForValue().setBit(redisKey3,4,true);  //设置某一位的值
		redisTemplate.opsForValue().setBit(redisKey3,5,true);
		redisTemplate.opsForValue().setBit(redisKey3,6,true);

		//做运算后需要用新的值接收

		String redisKey="test:bm:or";

		redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
				redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
						redisKey.getBytes(),redisKey1.getBytes(),redisKey2.getBytes(),redisKey3.getBytes());
				return redisConnection.bitCount(redisKey.getBytes());
			}
		});

		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 5));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 6));
	}

	//统计3组数据的布尔值,并对这三组数据做and运算
	@Test
	public void testBitMapOperation2(){
		String redisKey1="test:bm:01";

		//记录
		redisTemplate.opsForValue().setBit(redisKey1,0,true);  //设置某一位的值
		redisTemplate.opsForValue().setBit(redisKey1,1,true);
		redisTemplate.opsForValue().setBit(redisKey1,2,true);

		String redisKey2="test:bm:02";

		//记录
		redisTemplate.opsForValue().setBit(redisKey2,2,true);  //设置某一位的值
		redisTemplate.opsForValue().setBit(redisKey2,3,true);
		redisTemplate.opsForValue().setBit(redisKey2,4,true);

		String redisKey3="test:bm:03";

		//记录
		redisTemplate.opsForValue().setBit(redisKey3,4,true);  //设置某一位的值
		redisTemplate.opsForValue().setBit(redisKey3,5,true);
		redisTemplate.opsForValue().setBit(redisKey3,6,true);

		//做运算后需要用新的值接收

		String redisKey="test:bm:and";

		redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
				redisConnection.bitOp(RedisStringCommands.BitOperation.AND,
						redisKey.getBytes(),redisKey1.getBytes(),redisKey2.getBytes(),redisKey3.getBytes());
				return redisConnection.bitCount(redisKey.getBytes());
			}
		});

		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 5));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 6));
	}
}
