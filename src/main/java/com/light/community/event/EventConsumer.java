package com.light.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.light.community.entity.DiscussPost;
import com.light.community.entity.Event;
import com.light.community.entity.Message;
import com.light.community.service.DiscussPostService;
import com.light.community.service.ElasticsearchService;
import com.light.community.service.MessageService;
import com.light.community.util.CommunityConstant;
import com.light.community.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

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

	@Value("${qiniu.bucket.share.name}")
	private String shareBucketName;


	@Value("${qiniu.key.access}")
	private String accessKey;

	@Value("${qiniu.key.secret}")
	private String secretKey;

	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;


	//存储路径
	@Value("${wk.image.storage}")
	private String wkImageStorage;

	@Value("${wk.image.command}")
	private String wkImageCommand;




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

	//消费分享事件
	@KafkaListener(topics = TOPIC_SHARE)
	public void handleShare(ConsumerRecord record){
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

		String htmlUrl= (String) event.getData().get("htmlUrl");
		String fileName= (String) event.getData().get("fileName");
		String suffix= (String) event.getData().get("suffix");
		//生成长图命令
		String cmd=wkImageCommand+" --quality 75 "+htmlUrl+" "+wkImageStorage+"/"+fileName+suffix;
		try {
			Runtime.getRuntime().exec(cmd);
			logger.info("生成长图成功："+cmd);
		} catch (IOException e) {
			logger.error("生成长图失败："+e.getMessage());
		}

		//设置定时器，监视该图片，一旦生成长图，则上传至七牛云
		UploadTask task=new UploadTask(fileName,suffix);
		Future future = taskScheduler.scheduleAtFixedRate(task, 5000);//每5秒执行一次
		//启动定时器后会返回一个future，这个future中封装了任务的的状态，也可以用来停止定时器
		task.setFuture(future);//传入任务状态

	}

	class UploadTask implements Runnable{
		//文件名称
		private String fileName;
		//文件后缀
		private String suffix;
		//启动任务的返回值（可以用来停止定时器
		private Future future;
		//任务开始时间
		private long startTime;
		//任务上传次数
		private int uploadTimes;

		public UploadTask(String fileName, String suffix) {
			this.fileName = fileName;
			this.suffix = suffix;
			this.startTime=System.currentTimeMillis();
		}

		public void setFuture(Future future) {
			this.future = future;
		}

		@Override
		public void run() {
			//生成长图失败
			if(System.currentTimeMillis()-startTime>30000){//任务一旦运行超过30秒，则认为有问题
				logger.error("执行时间过长，终止任务："+fileName);
				future.cancel(true);
				return;

			}
			//上传七牛云失败
			if(uploadTimes>=3){
				logger.error("上传次数过多，终止任务："+future);
				future.cancel(true);
				return;
			}
			//尝试上传至七牛云
			//本地存放文件完整路径
			String path=wkImageStorage+"/"+fileName+suffix;
			File file=new File(path);
			if(file.exists()){
				logger.info(String.format("开始第%d次上传%s",++uploadTimes,fileName));
				//设置响应信息
				StringMap policy=new StringMap();
				policy.put("returnBody", CommunityUtil.getJsonString(0));
				//生成上传凭证
				Auth auth=Auth.create(accessKey,secretKey);
				String uploadToken=auth.uploadToken(shareBucketName,fileName,3600,policy);
				//指定上传机房
				UploadManager manager=new UploadManager(new Configuration(Region.region1()));
				try{
					//开始上传图片
					Response response=manager.put(
							path,fileName,uploadToken,null,"image/"+suffix.substring(suffix.lastIndexOf(".")+1),false
							);
					//处理响应结果
					JSONObject json = JSONObject.parseObject(response.bodyString());
					if(json==null||json.get("code")==null||!json.get("code").toString().equals("0")){
						logger.error(String.format("第%d上传失败【%s】",uploadTimes,fileName));
					}else {
						logger.info(String.format("第%d上传成功【%s】",uploadTimes,fileName));
						future.cancel(true);
					}
				}catch (QiniuException e){
					logger.error(String.format("第%d次上传失败【%s】",uploadTimes,fileName));
				}
			}else {
				logger.info("等待图片生成【"+fileName+"】");
			}

		}
	}
}
