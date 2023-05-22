package com.light.community.service;

import com.light.community.dao.MessageMapper;
import com.light.community.entity.Message;
import com.light.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author light
 * @Description
 * @create 2023-04-25 21:19
 */
@Service
public class MessageService {

	@Autowired
	private MessageMapper messageMapper;

	@Autowired
	private SensitiveFilter sensitiveFilter;

	//查询当前用户的会话列表，针对每个会话列表只返回最新的一条私信（支持分页
	public List<Message> findConversations(int userId, int offset, int limit){
		return messageMapper.selectConversations(userId,offset,limit);
	}

	//查询当前用户的会话数量
	public int findConversationCount(int userId){
		return messageMapper.selectConversationCount(userId);
	}

	//查询某个会话所包含的私信列表(支持分页
	public List<Message> findLetters(String conversationId,int offset,int limit){
		return messageMapper.selectLetters(conversationId,offset,limit);
	}

	//查询某个会话所包含的私信数量
	public int findLetterCount(String conversationId){
		return messageMapper.selectLetterCount( conversationId);
	}

	//查询未读的私信数量
	public int findLetterUnreadCount(int userId,String conversationId){
		return messageMapper.selectLetterUnreadCount(userId,conversationId);
	}

	//发送私信
	public int addMessage(Message message){
		//过滤标签
		message.setContent(HtmlUtils.htmlEscape(message.getContent()));
		//敏感词过滤
		message.setContent(sensitiveFilter.filter(message.getContent()));
		return messageMapper.insertMessage(message);
	}

	//设置消息状态
	public int readMessage(List<Integer> ids){
		return messageMapper.updateStatus(ids,1);
	}

	//查询某个主题下的最新通知
	public Message findLatestNotice(int userId,String topic){
		return messageMapper.selectLatestNotice(userId,topic);
	}

	//查询某个主题未读通知数
	//当topic==null时，查询的是所有通知数
	public int findNoticeUnreadCount(int userId,String topic){
		return messageMapper.selectNoticeUnreadCount(userId,topic);
	}

	//查询某个主题的所有通知数
	public int findNoticeCount(int userId,String topic){

		return messageMapper.selectNoticeCount(userId,topic);
	}

	//查询某个主题下的所有通知
	public List<Message> findNotices(int userId,String topic,int offset,int limit){
		return messageMapper.selectNotices(userId,topic,offset,limit);
	}


}
