package com.light.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.light.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author light
 * @Description  事件生产者
 * @create 2023-05-18 19:57
 */
@Component
public class EventProducer {
	@Autowired
	private KafkaTemplate kafkaTemplate;

	//处理事件
	public void fireEvent(Event event){
		//将事件发送出去:将事件对象转化成json字符串发送
		kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
	}
}
