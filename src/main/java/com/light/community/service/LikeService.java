package com.light.community.service;

import com.light.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @author light
 * @Description 点赞业务：点一下-->赞；点两下--->取消赞
 * @create 2023-05-04 20:12
 */

@Service
public class LikeService {
	@Autowired
	private RedisTemplate redisTemplate;


	//给帖子点赞
	public void like(int userId,int entityType,int entityId ,int entityUserId){
		//两次操作：一次记录点赞次数，另一次记录该实体获得的点赞数---->redis事务
		redisTemplate.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations operations) throws DataAccessException {
				//配置点赞key
				String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
				//配置用户获得赞的key
				String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
				Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
				//查询操作放在事务开启之前
				operations.multi();
				if(isMember){
					//判断该用户已点过赞
					operations.opsForSet().remove(entityLikeKey,userId);//取消赞
					operations.opsForValue().decrement(userLikeKey);//该实体用户获得的赞减一
				}else{
					operations.opsForSet().add(entityLikeKey,userId);//点赞
					operations.opsForValue().increment(userLikeKey);//该实体用户获得的赞加一
				}
				return operations.exec();
			}
		});



	}

	//查询某实体点赞的数量
	public long findEntityLikeCount(int entityType,int entityId){
		//配置点赞key
		String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
		return redisTemplate.opsForSet().size(entityLikeKey);
	}

	//查询某人对某实体点赞状态：已赞-->1;未赞--->0
	public int findEntityLikeStatus(int userId,int entityType,int entityId){
		//配置点赞key
		String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
		return redisTemplate.opsForSet().isMember(entityLikeKey,userId)?1:0;
	}

	//查询某个用户获得的赞
	public int findUserLikeCount(int userId){
		//配置用户获得赞的key
		String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);

		Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
		return count==null?0:count;
	}
}
