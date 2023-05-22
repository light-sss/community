package com.light.community.dao;

import com.light.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author light
 * @Description
 * @create 2023-04-21 13:15
 */

@Mapper
public interface CommentMapper {

	/**
	 *  通过Entity查询帖子评论
	 * @param entityType 评论类型：是对帖子的评论，对评论的评论
	 * @param entityId 评论id：是给哪个帖子的评论
	 * @param offset 分页显示，从第几页开始
	 * @param limit 显示多少条
	 * @return 返回查询的帖子列表
	 */
	List<Comment> selectCommentByEntity(int entityType,int entityId,int offset, int limit);

	//查询帖子总评论数
	int selectCountByEntity(int entityType,int entityId);


	//插入评论
	int insertComment(Comment comment);

	//通过id查找帖子
	Comment selectCommentById(int id);
}
