package com.light.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author light
 * @Description
 * @create 2023-05-17 20:52
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)//希望启用此配置类
public class KafkaTests {
	@Autowired
	private KafkaProducer kafkaProducer;
	@Test
	public void testKafka(){
		kafkaProducer.sendMessage("test","你好");
		kafkaProducer.sendMessage("test","hello kafka");
		try {
			Thread.sleep(1000*10);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}

//生产者
@Component
class KafkaProducer{
	@Autowired
	private KafkaTemplate kafkaTemplate;

	public void sendMessage(String topic,String content){
		kafkaTemplate.send(topic,content);
	}
}

//消费者
@Component
class KafkaConsumer{

	@KafkaListener(topics = {"test"})
	public void handleMessage(ConsumerRecord record){
		System.out.println(record.value());
	}
}
