package com.light.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.light.community.entity.DiscussPost;
import com.light.community.entity.Event;
import com.light.community.entity.Message;
import com.light.community.service.DiscussPostService;
import com.light.community.service.ElasticsearchService;
import com.light.community.service.MessageService;
import com.light.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author light
 * @Description 事件消费者
 * @create 2023-05-18 19:58
 */
@Component
public class EventConsumer implements CommunityConstant {
	//记录日志
	public static final Logger logger= LoggerFactory.getLogger(EventConsumer.class);

	@Autowired
	private MessageService messageService;

	@Autowired
	private DiscussPostService discussPostService;

	@Autowired
	private ElasticsearchService elasticsearchService;

	//消费者消费事件
	//最后将事件转化为消息插入到message中
	@KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})//监听的主题
	public void handleMessage(ConsumerRecord record){

		//先进行判断record是否为空:未发事件或者发送的事件为空
		if(record==null|| record.value()==null){
			logger.error("发送的消息为空！");
			return;
		}

		//事件不为空:将事件转换为Event对象
		Event event= JSONObject.parseObject(record.value().toString(),Event.class);
		//判断对象是否为空
		if(event==null){
			logger.error("消息格式错误！");
			return;
		}

		//将事件转化为消息：发送站内通知
		Message message=new Message();
		message.setFromId(SYSTEM_USER_ID);//系统发送的
		message.setToId(event.getEntityUserId()); //发给谁
		message.setConversationId(event.getTopic()); //发送的主题
		message.setCreateTime(new Date()); //发送时间

		//发送内容：要将内容拼装成发送的消息:xxx用户评论（点赞、关注）了你(的xxx
		Map<String,Object> content=new HashMap<>();
		content.put("userId",event.getUserId());//事件触发的人
		content.put("entityId",event.getEntityId()); //事件id
		content.put("entityType",event.getEntityType()); //事件类型

		//如果事件还有额外的信息
		if(!event.getData().isEmpty()){
			for(Map.Entry<String,Object> entry:event.getData().entrySet()){
				content.put(entry.getKey(),entry.getValue());
			}
		}

		//将消息内容转化为json字符串
		message.setContent(JSONObject.toJSONString(content));
		//将message对象存入message表中
		messageService.addMessage(message);
	}

	//消费者消费发帖事件--->同步到elasticsearch中
	@KafkaListener(topics = TOPIC_PUBLISH)
	public void handleDiscussPost(ConsumerRecord record){
		//先进行判断record是否为空:未发事件或者发送的事件为空
		if(record==null|| record.value()==null){
			logger.error("发送的消息为空！");
			return;
		}

		//事件不为空:将事件转换为Event对象
		Event event= JSONObject.parseObject(record.value().toString(),Event.class);
		//判断对象是否为空
		if(event==null){
			logger.error("消息格式错误！");
			return;
		}
		//从事件中获取帖子id
		DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
		//将查询到的帖子同步到elasticsearch中
		elasticsearchService.saveDiscussPost(post);
	}

	//消费者消费发帖事件--->同步到elasticsearch中
	@KafkaListener(topics = TOPIC_DELETE)
	public void handleDelete(ConsumerRecord record){
		//先进行判断record是否为空:未发事件或者发送的事件为空
		if(record==null|| record.value()==null){
			logger.error("发送的消息为空！");
			return;
		}

		//事件不为空:将事件转换为Event对象
		Event event= JSONObject.parseObject(record.value().toString(),Event.class);
		//判断对象是否为空
		if(event==null){
			logger.error("消息格式错误！");
			return;
		}

		elasticsearchService.deleteDiscussPost(event.getEntityId());
	}
}
