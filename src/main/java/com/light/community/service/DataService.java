package com.light.community.service;

import com.light.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author light
 * @Description  统计uv，dau
 * @create 2023-06-11 20:01
 */
@Service
public class DataService {
	@Autowired
	private RedisTemplate redisTemplate;

	private static SimpleDateFormat df=new SimpleDateFormat("yyyyMMdd");   //统一处理日期格式

	//将指定的ip计入uv
	//使用hyperloglog
	public void recordUV(String ip){
		//构建key
		String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));

		redisTemplate.opsForHyperLogLog().add(redisKey,ip);
	}

	//统计指定范围内的uv
	public long calculateUV(Date start,Date end){
		if(start==null||end==null){
			throw new RuntimeException("参数不能为空！");
		}
		//获取指定日期内key的集合
		List<String> keyList=new ArrayList<>();
		//获取获取系统的当前日历对象
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(start);//将时间对象data设置为新的日历
		while(!calendar.getTime().after(end)){ //从当前时间到end之前的这一段时间
			//获取当前时间的key，并将key装到集合中去
			String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
			keyList.add(key);
			calendar.add(calendar.DATE,1);
		}

		//合并这些数据，需要用新的值去接收
		String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
		redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray());
		return redisTemplate.opsForHyperLogLog().size(redisKey);
	}

	//将用户计入dau中
	//使用bitmap

	public void recordDAU(int userId){
		//构建key
		String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));

		redisTemplate.opsForValue().setBit(redisKey,userId,true);
	}


	//获取指定日期内的dau
	public long calculateDAU(Date start,Date end){
		if(start==null||end==null){
			throw new RuntimeException("参数不能为空！");
		}
		List<byte[]> keyList=new ArrayList<>();
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(start);
		while(!calendar.getTime().after(end)){
			String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
			keyList.add(key.getBytes());
			calendar.add(calendar.DATE,1);
		}

		//做or运算
		return (long) redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
				String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
				redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
						redisKey.getBytes(),keyList.toArray(new byte[0][0]));//返回指定类型的数组
				return redisConnection.bitCount(redisKey.getBytes());
			}
		});
	}
}
