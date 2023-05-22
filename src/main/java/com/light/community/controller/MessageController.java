package com.light.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.light.community.entity.Message;
import com.light.community.entity.Page;
import com.light.community.entity.User;
import com.light.community.service.MessageService;
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
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @author light
 * @Description
 * @create 2023-04-25 21:52
 */
@Controller
public class MessageController implements CommunityConstant {
	@Autowired
	private MessageService messageService;

	@Autowired
	private HostHolder hostHolder;

	@Autowired
	private UserService userService;



	@RequestMapping(path = "/letter/list",method = RequestMethod.GET)
	public String findLetterList(Model model, Page page){

		User user = hostHolder.getUser();
		//设置分页信息
		page.setLimit(5);//一页显示五条
		page.setPath("/letter/list");//设置分页路径
		page.setRows(messageService.findConversationCount(user.getId()));//设置总数据数

		//查询私信列表
		List<Message> conversationsList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
		List<Map<String,Object>> conversations=new ArrayList<>();
		if(conversationsList!=null){
			for(Message message:conversationsList){

				Map<String,Object> map =new HashMap<>();
				map.put("conversation",message); //会话信息
				map.put("letterCount",messageService.findLetterCount(message.getConversationId()));//会话消息数
				map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));//未读消息数
				int targetId= user.getId()==message.getFromId()? message.getToId() : message.getFromId();
				map.put("target",userService.findUserById(targetId));//装配用户信息

				conversations.add(map);

			}
		}
		model.addAttribute("conversations",conversations);

		//查询列表未读消息
		int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
		model.addAttribute("letterUnreadCount",letterUnreadCount);
		//查询全部通知的未读消息数
		int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
		model.addAttribute("noticeUnreadCount",noticeUnreadCount);

		return "/site/letter";
	}

	//查看私信详情
	@RequestMapping(value = "/letter/detail/{conversationId}",method = RequestMethod.GET)
	public String showLetterDetail(@PathVariable("conversationId") String conversationId,Model model,Page page){
		//显示分页信息
		page.setLimit(5);
		page.setPath("/letter/detail/" + conversationId);
		page.setRows(messageService.findLetterCount(conversationId));
		//会话列表
		List<Message> letterList=messageService.findLetters(conversationId, page.getOffset(), page.getLimit());

		List<Map<String,Object>> letters=new ArrayList<>();
		if(letterList!=null){
			for(Message message:letterList){
				Map<String,Object> map=new HashMap<>();
				map.put("letter",message);
				map.put("fromUser",userService.findUserById(message.getFromId()));
				letters.add(map);
			}
		}
		model.addAttribute("letters",letters);
		//获取私信目标
		User target = getTarget(conversationId);
		model.addAttribute("target",target);

		//更改私信状态
		List<Integer> ids = getLetterIds(letterList);
		if(!ids.isEmpty()){

			messageService.readMessage(ids);
		}

		return "/site/letter-detail";
	}

	//获取私信目标
	private User getTarget(String conversationId){
		String[] ids = conversationId.split("_");
		int id0=Integer.parseInt(ids[0]);
		int id1=Integer.parseInt(ids[1]);

		if(hostHolder.getUser().getId()==id0){
			return userService.findUserById(id1);
		}else{
			return userService.findUserById(id0);
		}
	}

	//获取未读私信id列表
	private List<Integer> getLetterIds(List<Message> letterList){
		List<Integer> ids=new ArrayList<>();
		if(!letterList.isEmpty()){
			for(Message message:letterList){
				if(hostHolder.getUser().getId()==message.getToId()&&message.getStatus()==0){
					ids.add(message.getId());
				}
			}
		}
		return ids;
	}


	//发送私信
	@RequestMapping(value = "/letter/send",method = RequestMethod.POST)
	@ResponseBody
	public String sendLetter(String toName,String content){

		//获取发送目标
		User target = userService.selectUserByName(toName);
		if(target==null){
			return CommunityUtil.getJsonString(1,"目标用户不存在！");
		}
		//获取发送的私信内容
		Message message=new Message();
		message.setContent(content); //发送的内容
		message.setToId(target.getId()); //收私信的id
		message.setFromId(hostHolder.getUser().getId()); //发私信的id
		message.setStatus(0); //私信状态
		message.setCreateTime(new Date()); //设置发送时间
		//设置会话id
		if(message.getFromId()< message.getToId()){
			message.setConversationId(message.getFromId()+"_"+ message.getToId());
		}else{
			message.setConversationId(message.getToId()+"_"+ message.getFromId());
		}
		messageService.addMessage(message);//将私信内容保存至数据库中
		return CommunityUtil.getJsonString(0);
	}

	//显示通知页面
	@RequestMapping(value = "/notice/list",method = RequestMethod.GET)
	public String getNoticeList(Model model){
		User user = hostHolder.getUser();

		//查询评论类通知
		Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
		Map<String,Object> messageVO=new HashMap<>();
		messageVO.put("message",message);
		if(message!=null){

			//将content内容还原
			String content = HtmlUtils.htmlUnescape(message.getContent());
			Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);

			messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
			messageVO.put("entityType",data.get("entityType"));
			messageVO.put("entityId",data.get("entityId"));
			messageVO.put("postId",data.get("postId"));

			//评论通知数量、未读通知数量
			int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
			int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
			messageVO.put("count",count);
			messageVO.put("unread",unread);

		}
		model.addAttribute("commentNotice",messageVO);
		//查询点赞类通知
		message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
		messageVO=new HashMap<>();
		messageVO.put("message",message);
		if(message!=null){

			//将content内容还原
			String content = HtmlUtils.htmlUnescape(message.getContent());
			Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
			messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
			messageVO.put("entityType",data.get("entityType"));
			messageVO.put("entityId",data.get("entityId"));
			messageVO.put("postId",data.get("postId"));

			//评论通知数量、未读通知数量
			int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
			int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
			messageVO.put("count",count);
			messageVO.put("unread",unread);

		}
		model.addAttribute("likeNotice",messageVO);

		//查询关注类通知
		message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
		messageVO=new HashMap<>();
		messageVO.put("message",message);
		if(message!=null){
			//将content内容还原
			String content = HtmlUtils.htmlUnescape(message.getContent());
			Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
			messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
			messageVO.put("entityType",data.get("entityType"));
			messageVO.put("entityId",data.get("entityId"));

			//评论通知数量、未读通知数量
			int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
			int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
			messageVO.put("count",count);
			messageVO.put("unread",unread);

		}
		model.addAttribute("followNotice",messageVO);

		//查询全部通知的未读消息数
		int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
		model.addAttribute("noticeUnreadCount",noticeUnreadCount);
		//查询全部私信的未读消息数
		int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
		model.addAttribute("letterUnreadCount",letterUnreadCount);

		return "/site/notice";
	}

	@RequestMapping(value = "/notice/detail/{topic}",method = RequestMethod.GET)
	public String getNoticeDetail(@PathVariable("topic") String topic,Page page,Model model){
		User user = hostHolder.getUser();
		//设置分页
		page.setRows(5);
		page.setPath("/notice/detail/"+topic);
		page.setRows(messageService.findNoticeCount(user.getId(),topic));


		List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
		List<Map<String,Object>> noticeVoList=new ArrayList<>();
		if(noticeList!=null){

			for(Message notice:noticeList){
				Map<String,Object> map=new HashMap<>();
				//通知
				map.put("notice",notice);
				//通知内容
				String content = HtmlUtils.htmlUnescape(notice.getContent());
				HashMap<String,Object> data = JSONObject.parseObject(content, HashMap.class);
				map.put("user",userService.findUserById((Integer) data.get("userId")));
				map.put("entityType",data.get("entityType"));
				map.put("entityId",data.get("entityId"));
				map.put("postId",data.get("postId"));
				//通知作者
				map.put("fromUser",userService.findUserById(notice.getFromId()));
				noticeVoList.add(map);
			}
		}
		model.addAttribute("notices",noticeVoList);

		//设置已读
		List<Integer> ids=getLetterIds(noticeList);
		if(!ids.isEmpty()){
			messageService.readMessage(ids);
		}

		return "/site/notice-detail";
	}

}
