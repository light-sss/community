package com.light.community.service;

import com.light.community.dao.CommentMapper;
import com.light.community.dao.DiscussPostMapper;
import com.light.community.entity.DiscussPost;
import com.light.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author light
 * @Description
 * @create 2023-03-23 16:36
 */
@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;


    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode) {
        return discussPostMapper.selectDiscussPost(userId, offset, limit,orderMode);
    }

    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    //插入帖子
    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        //转移HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //对post中的数据进行敏感词过滤
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        return discussPostMapper.insertDiscussPost(post);
    }

    //查询帖子
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    //添加帖子评论数量
    public int updateComment(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }


    //置顶:更改类型为1
    public int updateType(int id,int type){
        return discussPostMapper.updateType(id,type);
    }

    //加精：更改状态为1
    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id,status);
    }


    //更改帖子分数
    public int updateScore(int id,double score){
        return discussPostMapper.updateScore(id,score);
    }
}


