package com.light.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author light
 * @Description
 * @create 2023-05-18 16:36
 */
public class Event {
	private String topic; //事件类型（话题
	private int userId;// 事件的触发者
	private int entityType; //实体类型：帖子、评论
	private int entityId; //实例id：帖子id、评论id
	private int entityUserId; //实体作者：帖子作者、评论作者
	//封装其他信息
	private Map<String,Object> data=new HashMap<>();

	public String getTopic() {
		return topic;
	}

	public Event setTopic(String topic) {
		this.topic = topic;
		return  this;
	}

	public int getUserId() {
		return userId;
	}

	public Event setUserId(int userId) {
		this.userId = userId;
		return this;
	}

	public int getEntityType() {
		return entityType;
	}

	public Event setEntityType(int entityType) {
		this.entityType = entityType;
		return this;
	}

	public int getEntityId() {
		return entityId;
	}

	public Event setEntityId(int entityId) {
		this.entityId = entityId;
		return this;
	}

	public int getEntityUserId() {
		return entityUserId;
	}

	public Event setEntityUserId(int entityUserId) {
		this.entityUserId = entityUserId;
		return this;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public Event setData(String key,Object value) {
		this.data.put(key,value);
		return this;
	}
}
