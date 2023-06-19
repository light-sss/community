package com.light.community.controller;

import com.light.community.entity.Event;
import com.light.community.entity.User;
import com.light.community.event.EventProducer;
import com.light.community.service.LikeService;
import com.light.community.util.CommunityConstant;
import com.light.community.util.CommunityUtil;
import com.light.community.util.HostHolder;
import com.light.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author light
 * @Description
 * @create 2023-05-04 20:28
 */
@Controller
public class LikeController implements CommunityConstant {

	@Autowired
	private LikeService likeService;
	@Autowired
	private HostHolder hostHolder;

	@Autowired
	private EventProducer eventProducer;

	@Autowired
	private RedisTemplate redisTemplate;

	@RequestMapping(value = "/like",method = RequestMethod.POST)
	@ResponseBody
	public String like(int entityType,int entityId,int entityUserId,int postId){
		User user = hostHolder.getUser();
		//点赞
		likeService.like(user.getId(),entityType,entityId, entityUserId);

		//获取点赞数量
		long likeCount = likeService.findEntityLikeCount(entityType, entityId);
		//获取点赞状态
		int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
		//返回的结果
		Map<String,Object> map=new HashMap<>();
		map.put("likeCount",likeCount);
		map.put("likeStatus",likeStatus);

		//触发点赞事件
		if(likeStatus==1){
			Event event=new Event()
					.setTopic(TOPIC_LIKE)
					.setUserId(hostHolder.getUser().getId())
					.setEntityId(entityId)
					.setEntityType(entityType)
					.setEntityUserId(entityUserId)
					.setData("postId",postId);

			eventProducer.fireEvent(event);
		}

		//计算对帖子点赞的情况
		if(entityType==ENTITY_TYPE_POST){
			//计算帖子分数(将要计算分数的帖子加入set中
			String redisKey = RedisKeyUtil.getPostScoreKey();
			redisTemplate.opsForSet().add(redisKey,postId);

		}

		return CommunityUtil.getJsonString(0,null,map);
	}

}
