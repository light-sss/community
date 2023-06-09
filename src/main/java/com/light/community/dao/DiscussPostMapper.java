package com.light.community.dao;

import com.light.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * @author light
 * @Description
 * @create 2023-03-23 15:58
 *
 *///必须加一个注解才能让Spring容器装配bean
//@Repository:开发的是数据库访问的组件（这个注解也可以）
@Mapper//mybatis注解
public interface DiscussPostMapper {

    /**
     * 对查询方法进行重构
     * @param userId
     * @param offset
     * @param limit
     * @param orderMode  当前帖子排序状态：orderMode=0：默认；orderMode=1：按最热来排
     * @return
     */
    //声明查询方法：分页查询，返回多条记录
    List<DiscussPost> selectDiscussPost(int userId,int offset,int limit,int orderMode);


    //查询出表里一共有多少条数据   @Param("userId"):为参数取别名
    int selectDiscussPostRows(@Param("userId")int userId);
    //如果需要动态的提出条件（在<if>里使用），并且需要用到这个参数，且这个方法有且只有一个条件参数，此参数必须取别名

    //实现插入帖子功能
    int insertDiscussPost(DiscussPost discussPost);

    //查找帖子-->通过id
    DiscussPost selectDiscussPostById(int id);

    //增加帖子评论数量
    int updateCommentCount(int id,int commentCount);

    //置顶:更改类型为1
    int updateType(int id,int type);

    //加精：更改状态为1
    int updateStatus(int id ,int status);

    //更改帖子分数
    int updateScore(int id,double score);

}
