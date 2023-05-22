package com.light.community.controller;

import com.light.community.entity.Event;
import com.light.community.entity.Page;
import com.light.community.entity.User;
import com.light.community.event.EventProducer;
import com.light.community.service.FollowService;
import com.light.community.service.UserService;
import com.light.community.util.CommunityConstant;
import com.light.community.util.CommunityUtil;
import com.light.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author light
 * @Description
 * @create 2023-05-07 22:20
 */

@Controller
public class FollowController implements CommunityConstant {
	@Autowired
	private FollowService followService;

	@Autowired
	private HostHolder hostHolder;

	@Autowired
	private UserService userService;

	@Autowired
	private EventProducer eventProducer;
	//关注
	@RequestMapping(value = "/follow",method = RequestMethod.POST)
	@ResponseBody //异步请求，局部刷新
	public String follow(int entityType,int entityId){
		User user = hostHolder.getUser();
		followService.follow(user.getId(), entityType,entityId);

		//触发关注事件
		Event event=new Event()
				.setTopic(TOPIC_FOLLOW)
				.setUserId(user.getId())
				.setEntityId(entityId)
				.setEntityType(entityType)
				.setEntityUserId(entityId);
		eventProducer.fireEvent(event);

		return CommunityUtil.getJsonString(0,"已关注！");
	}

	//取消关注
	@RequestMapping(value = "/unfollow",method = RequestMethod.POST)
	@ResponseBody //异步请求，局部刷新
	public String unfollow(int entityType,int entityId){
		User user = hostHolder.getUser();
		followService.unfollow(user.getId(), entityType,entityId);

		return CommunityUtil.getJsonString(0,"已取消关注！");
	}

	//用户关注的人
	@RequestMapping(value = "/followees/{userId}",method = RequestMethod.GET)
	public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
		User user = userService.findUserById(userId);
		if(user==null){
			throw new RuntimeException("该用户不存在！");
		}
		model.addAttribute("user",user);
		page.setLimit(5);
		page.setPath("/followees/"+userId);
		page.setRows((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

		List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
		if(userList!=null){
			//查看当前登录用户是否关注用户关注的人
			for(Map<String, Object> map:userList){
				User u = (User) map.get("user");
				map.put("hasFollowed",hasFollowed(u.getId()));
			}
		}

		model.addAttribute("users",userList);

		return "/site/followee";
	}

	//用户的粉丝
	@RequestMapping(value = "/followers/{userId}",method = RequestMethod.GET)
	public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
		User user = userService.findUserById(userId);
		if(user==null){
			throw new RuntimeException("该用户不存在！");
		}
		model.addAttribute("user",user);
		page.setLimit(5);
		page.setPath("/followers/"+userId);
		page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

		List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
		if(userList!=null){
			//查看当前登录用户是否关注用户关注的人
			for(Map<String, Object> map:userList){
				User u = (User) map.get("user");
				map.put("hasFollowed",hasFollowed(u.getId()));
			}
		}

		model.addAttribute("users",userList);

		return "/site/follower";
	}

	//查看当前登录用户是否关注用户关注的人
	private boolean hasFollowed(int userId){
		if(hostHolder.getUser()==null){
			return false;
		}

		return followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
	}
}
