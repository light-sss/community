package com.light.community.controller;

import com.light.community.entity.Comment;
import com.light.community.entity.DiscussPost;
import com.light.community.entity.Page;
import com.light.community.entity.User;
import com.light.community.service.CommentService;
import com.light.community.service.DiscussPostService;
import com.light.community.service.LikeService;
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

import java.util.*;

/**
 * @author light
 * @Description
 * @create 2023-04-19 13:51
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
	@Autowired
	private DiscussPostService discussPostService;
	@Autowired
	private HostHolder hostHolder;

	@Autowired
	private UserService userService;

	@Autowired
	private LikeService likeService;

	@Autowired
	private CommentService commentService;

	@RequestMapping(value = "/add",method = RequestMethod.POST)
	@ResponseBody
	public String addDiscussPost(String title,String content){
		//判断是否登录
		User user = hostHolder.getUser();
		if(user==null){
			//还未登陆无权限访问
			return CommunityUtil.getJsonString(403,"还未登陆！");
		}
		DiscussPost post=new DiscussPost();
		post.setUserId(user.getId());
		post.setTitle(title);
		post.setContent(content);
		post.setCreateTime(new Date());
		discussPostService.addDiscussPost(post);
		return CommunityUtil.getJsonString(0,"发布成功！");
	}


	@RequestMapping(value = "/detail/{id}" ,method = RequestMethod.GET)
	public String findDiscussPosts(@PathVariable int id, Model model, Page page){
		//帖子
		DiscussPost post = discussPostService.findDiscussPostById(id);
		model.addAttribute("post",post);

		//发帖作者（帖子内还要显示作者相关信息：后续可以通过redis数据库进行访问
		User user = userService.findUserById(post.getUserId());
		model.addAttribute("user",user);
		//点赞数量
		long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, id);
		model.addAttribute("likeCount",likeCount);

		//点赞状态:未登录状态只显示赞的数量不显示赞的状态
		int likeStatus = hostHolder.getUser()==null?0:
				likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, id);

		model.addAttribute("likeStatus",likeStatus);

		//评论分页信息
		page.setLimit(5); //一页显示五条评论
		page.setPath("/discuss/detail/"+id);//设置分页查询路径
		page.setRows(post.getCommentCount());//一共有多少条帖子
		// 评论: 给帖子的评论
		// 回复: 给评论的评论
		// 评论列表
		List<Comment> commentList = commentService.findCommentByEntity(
				ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
		//评论VO列表
		List<Map<String,Object>> commentVOList=new ArrayList<>();
		if(commentList != null){

			for(Comment comment : commentList){

				Map<String,Object> commentVO=new HashMap<>();
				//评论
				commentVO.put("comment",comment);//加入评论信息
				//将评论的作者也加入map中
				commentVO.put("user",userService.findUserById(comment.getUserId()));

				//点赞数量
				 likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
				commentVO.put("likeCount",likeCount);

				//点赞状态:未登录状态只显示赞的数量不显示赞的状态
				 likeStatus = hostHolder.getUser()==null?0:
						likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());

				commentVO.put("likeStatus",likeStatus);

				//回复
				List<Comment> replyList = commentService.findCommentByEntity(
						ENTITY_TYPE_COMMENT, comment.getId(),0, Integer.MAX_VALUE);
				//回复VO列表
				List<Map<String,Object>> replyVOList=new ArrayList<>();
				if(replyList!=null){
					for(Comment reply:replyList){

						Map<String,Object> replyVO=new HashMap<>();
						//加入回复信息
						replyVO.put("reply",reply);
						//加入回复作者
						replyVO.put("user",userService.findUserById(reply.getUserId()));
						//回复目标：给谁回复
						User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
						replyVO.put("target",target);

						//点赞数量
						likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
						replyVO.put("likeCount",likeCount);

						//点赞状态:未登录状态只显示赞的数量不显示赞的状态
						likeStatus = hostHolder.getUser()==null?0:
								likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());

						replyVO.put("likeStatus",likeStatus);


						replyVOList.add(replyVO);

					}

				}
				commentVO.put("replys",replyVOList);
				//回复数量
				int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
				commentVO.put("replyCount",replyCount);
				commentVOList.add(commentVO);

			}

		}

		model.addAttribute("comments",commentVOList);
		return "/site/discuss-detail";
	}


}