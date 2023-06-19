package com.light.community.quartz;

import com.light.community.entity.DiscussPost;
import com.light.community.service.DiscussPostService;
import com.light.community.service.ElasticsearchService;
import com.light.community.service.LikeService;
import com.light.community.util.CommunityConstant;
import com.light.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author light
 * @Description  定义计算帖子分数任务
 * @create 2023-06-18 14:54
 */


/**
 * Nowcoder
 *
 * log(精华分+评论数*10+ 点赞数*2) +(发布时间 - 牛客纪元)
 */
public class PostScoreRefreshJob implements Job , CommunityConstant {

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private LikeService likeService;

	@Autowired
	private DiscussPostService discussPostService;

	@Autowired
	private ElasticsearchService elasticsearchService;

	private static final Logger logger= LoggerFactory.getLogger(PostScoreRefreshJob.class);

	//牛客纪元
	private static final Date epoch;

	static {
		try {
			epoch=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
		} catch (ParseException e) {
			throw new RuntimeException("初始化牛客纪元失败！",e);
		}
	}

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		String redisKey = RedisKeyUtil.getPostScoreKey();
		///BoundSetOperations就是一个绑定key的对象，我们可以通过这个对象来进行与key相关的操作。
		BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

		if(operations.size()==0){
			logger.info("[任务取消] 没有要刷新的帖子！");
			return;
		}

		logger.info("[任务开始] 正在刷新帖子分数："+operations.size());


		while(operations.size()>0){
			this.refresh((Integer) operations.pop()); //根据帖子id查找帖子计算分数
		}

		logger.info("[任务结束] 帖子分数刷新完毕！");

	}

	private void refresh(int postId){


		DiscussPost post = discussPostService.findDiscussPostById(postId);
		if(post==null){
			logger.error("该帖子不存在！");
			return;
		}
		if(post.getStatus()==2){
			logger.error("该帖子已被删除！");
			return;
		}
		//是否精华
		boolean wonderful = post.getStatus() == 1;
		//评论数量
		int commentCount = post.getCommentCount();
		//点赞数量
		long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

		//log(精华分+评论数*10+ 点赞数*2) +(发布时间 - 牛客纪元)
		//计算权重
		long w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
		//分数
		double score = Math.log10(Math.max(1, w)) + (post.getCreateTime().getTime() - epoch.getTime()) / (3600 * 24 * 1000);

		//更新帖子分数
		discussPostService.updateScore(postId,score);

		//同步elasticsearch中搜索数据
		post.setScore(score);
		elasticsearchService.saveDiscussPost(post); //这个实体是早先查到的，中途更改了它的分数，所以需要将分数同步一下


	}
}
