package com.light.community.service;

import com.light.community.entity.User;
import com.light.community.util.CommunityConstant;
import com.light.community.util.HostHolder;
import com.light.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author light
 * @Description 开发关注、取关业务
 * @create 2023-05-07 21:50
 */
@Service
public class FollowService implements CommunityConstant {

	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private UserService userService;

	@Autowired
	private HostHolder hostHolder;


	//关注
	public void follow(int userId,int entityType,int entityId){
		redisTemplate.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations operations) throws DataAccessException {
				//构造followeeKey
				String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
				//构造followerKey
				String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
				operations.multi(); //开启事务

				//followee:userId:entityType---->zset(entityId,now)
				redisTemplate.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
				//follower:entityType:entityId---->zset(userId,now)
				redisTemplate.opsForZSet().add(followerKey,userId,System.currentTimeMillis());

				return operations.exec();
			}
		});
	}
	//取消关注
	public void unfollow(int userId,int entityType,int entityId){
		redisTemplate.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations operations) throws DataAccessException {
				//构造followeeKey
				String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
				//构造followerKey
				String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
				operations.multi(); //开启事务

				//followee:userId:entityType---->zset(entityId,now)
				redisTemplate.opsForZSet().remove(followeeKey,entityId,System.currentTimeMillis());
				//follower:entityType:entityId---->zset(userId,now)
				redisTemplate.opsForZSet().remove(followerKey,userId,System.currentTimeMillis());

				return operations.exec();
			}
		});
	}

	//查询实体关注的数量
	public long findFolloweeCount(int userId,int entityType){
		//构造followeeKey
		String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
		return redisTemplate.opsForZSet().zCard(followeeKey);
	}

	//查询实体的粉丝数量
	public long findFollowerCount(int entityType,int entityId){
		//构造followerKey
		String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
		return redisTemplate.opsForZSet().zCard(followerKey);
	}

	//查询当前用户是否已关注该实体
	public boolean hasFollowed(int userId,int entityType,int entityId){
		//查询该用户的关注中是否有该实体id--->即是否有该实体的分数
		//构造followeeKey
		String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
		return redisTemplate.opsForZSet().score(followeeKey,entityId)!=null;
	}

	//查询实体关注的人
	public List<Map<String,Object>> findFollowees(int userId,int offset,int limit){
		//构造followeeKey
		String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
		//找到关注的人的id列表---->倒叙显示
		Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
		if(targetIds==null){
			return null;
		}
		List<Map<String,Object>> list=new ArrayList<>();
		for(Integer targetId:targetIds){
			Map<String,Object> map=new HashMap<>();
			//通过id查到对应用户
			User user = userService.findUserById(targetId);
			map.put("user",user);
			//查到用户关注时间
			Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
			map.put("followTime",new Date(score.longValue()));
			list.add(map);
		}
		return list;
	}

	//查询用户的粉丝
	public List<Map<String,Object>> findFollowers(int userId,int offset,int limit){
		//构造followerKey
		String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
		//找到关注的人的id列表---->倒叙显示
		Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
		if(targetIds==null){
			return null;
		}
		List<Map<String,Object>> list=new ArrayList<>();
		for(Integer targetId:targetIds){
			Map<String,Object> map=new HashMap<>();
			//通过id查到对应用户
			User user = userService.findUserById(targetId);
			map.put("user",user);
			//查到用户关注时间
			Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
			map.put("followTime",new Date(score.longValue()));
			list.add(map);
		}
		return list;
	}



}
