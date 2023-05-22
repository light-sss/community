package com.light.community.service;

import com.light.community.dao.CommentMapper;
import com.light.community.dao.DiscussPostMapper;
import com.light.community.entity.Comment;
import com.light.community.util.CommunityConstant;
import com.light.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author light
 * @Description
 * @create 2023-04-21 13:38
 */
@Service
public class CommentService implements CommunityConstant {
	@Autowired
	private CommentMapper commentMapper;

	@Autowired
	private SensitiveFilter sensitiveFilter;

	@Autowired
	private DiscussPostMapper discussPostMapper;

	public List<Comment> findCommentByEntity(int entityType,int entityId,int offset, int limit){
		return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
	}

	public int findCountByEntity(int entityType,int entityId){
		return commentMapper.selectCountByEntity(entityType,entityId);
	}


	//先添加帖子评论，再添加评论数量（开启事务：要么都做，要么都不做）
	@Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
	public int addComment(Comment comment){
		if(comment==null){
			throw new IllegalArgumentException("参数为空！");
		}

		//先增加评论
		comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));//过滤标签标识
		comment.setContent(sensitiveFilter.filter(comment.getContent()));//过滤敏感词
		int rows = commentMapper.insertComment(comment);

		//更新帖子评论数量
		//判断评论帖子类型
		if(comment.getEntityType()==ENTITY_TYPE_POST){
			int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
			//entityId:给哪个帖子的评论
			discussPostMapper.updateCommentCount(comment.getEntityId(),count);
		}

		return rows;
	}

	public Comment findCommentById(int id){
		return commentMapper.selectCommentById(id);
	}
}
