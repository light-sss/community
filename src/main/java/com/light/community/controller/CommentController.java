package com.light.community.controller;

import com.light.community.entity.Comment;
import com.light.community.entity.DiscussPost;
import com.light.community.entity.Event;
import com.light.community.event.EventProducer;
import com.light.community.service.CommentService;
import com.light.community.service.DiscussPostService;
import com.light.community.util.CommunityConstant;
import com.light.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @author light
 * @Description
 * @create 2023-04-23 14:13
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
	@Autowired
	private HostHolder hostHolder;
	@Autowired
	private CommentService commentService;

	@Autowired
	private DiscussPostService discussPostService;

	@Autowired
	private EventProducer eventProducer;

	@RequestMapping(value = "/add/{discussPostId}",method = RequestMethod.POST)
	public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
		comment.setStatus(0);
		comment.setCreateTime(new Date());
		comment.setUserId(hostHolder.getUser().getId());
		commentService.addComment(comment);

		//发送评论后发送通知通知对方
		Event event=new Event()
				.setTopic(TOPIC_COMMENT)
				.setUserId(hostHolder.getUser().getId())
				.setEntityType(comment.getEntityType())
				.setEntityId(comment.getEntityId())
				.setData("postId",discussPostId); //最终发送的消息还有点击查看，要链接到帖子详情页面，需要知道帖子id

		//帖子的作者：评论的是帖子帖子--》帖子表；  评论的是评论---》评论表
		if(comment.getEntityType()==ENTITY_TYPE_POST){
			DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
			event.setEntityUserId(target.getUserId());
		} else if (comment.getEntityType()==ENTITY_TYPE_COMMENT) {
			Comment target = commentService.findCommentById(comment.getEntityId());
			event.setEntityUserId(target.getUserId());
		}

		//生产者发送实现
		eventProducer.fireEvent(event);

		return "redirect:/discuss/detail/" + discussPostId;

	}
}
