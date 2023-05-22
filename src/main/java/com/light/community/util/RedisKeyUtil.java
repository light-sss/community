package com.light.community.util;

/**
 * @author light
 * @Description
 * @create 2023-05-04 20:07
 */
public class RedisKeyUtil {

	private static final String SPLIT=":";
	private static final String PREFIX_ENTITY_LIKE="like:entity";
	private static final String PREFIX_USER_LIKE="like:user";
	private static final String PREFIX_FOLLOWEE="followee";  //关注的目标
	private static final String PREFIX_FOLLOWER="follower";  //粉丝
	public static final String PREFIX_KAPTCHA="kaptcha"; //验证码
	public static final String PREFIX_TICKET="ticket"; //登录凭证
	public static final String PREFIX_USER="user"; //缓存用户


	//某个实体的赞
	//like:entity:entityType:entityId----->set(userId)
	public static String getEntityLikeKey(int entityType,int entityId){
		return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
	}

	//某个用户获得的赞
	//like:user:userId---->int
	public static String getUserLikeKey(int userId){
		return PREFIX_USER_LIKE+SPLIT+userId;
	}

	//某个用户关注的实体:
	//followee:userId:entityType---->zset(entityId,now)
	public static String getFolloweeKey(int userId,int entityType){
		return PREFIX_FOLLOWEE+SPLIT+userId+SPLIT+entityType;
	}

	//某个实体的粉丝
	//follower:entityType:entityId---->zset(userId,now)
	public static String getFollowerKey(int entityType,int entityId){
		return PREFIX_FOLLOWER+SPLIT+entityType+SPLIT+entityId;
	}

	//获取验证码的key:当用户到登录页面时，需要给验证码下发一个临时凭证来进行验证码归属
	public static String getKaptchaKey(String owner){
		return PREFIX_KAPTCHA+SPLIT+owner;
	}

	//获取登录凭证key
	public static String getTicketKey(String ticket){
		return PREFIX_TICKET+SPLIT+ticket;
	}

	//获取用户key
	public static String getUserKey(int userId){
		return PREFIX_USER+SPLIT+userId;
	}

}
