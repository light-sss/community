package com.light.community.dao;

import com.light.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author light
 * @Description
 * @create 2023-04-24 14:11
 */

@Mapper
public interface MessageMapper {
	//查询当前用户的会话列表，针对每个会话列表只返回最新的一条私信（支持分页
	List<Message> selectConversations(int userId,int offset,int limit);

	//查询当前用户的会话数量
	int selectConversationCount(int userId);

	//查询某个会话所包含的私信列表(支持分页
	List<Message> selectLetters(String conversationId,int offset,int limit);

	//查询某个会话所包含的私信数量
	int selectLetterCount(String conversationId);

	//查询未读的私信数量
	int selectLetterUnreadCount(int userId,String conversationId);

	//新增消息（发送私信
	int insertMessage(Message message);

	//更改消息状态  未读--->已读--->status:0
	int updateStatus(List<Integer> ids,int status);

	//查询某个主题下的最新通知
	Message selectLatestNotice(int userId,String topic);

	//查询某个主题未读通知数
	//当topic==null时，查询的是所有通知数
	int selectNoticeUnreadCount(int userId,String topic);

	//查询某个主题的所有通知数
	int selectNoticeCount(int userId,String topic);

	//查询某个主题下的所有通知
	 List<Message> selectNotices(int userId,String topic,int offset,int limit);
}
