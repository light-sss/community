package com.light.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.light.community.dao.CommentMapper;
import com.light.community.dao.DiscussPostMapper;
import com.light.community.entity.DiscussPost;
import com.light.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author light
 * @Description
 * @create 2023-03-23 16:36
 */
@Service
public class DiscussPostService {

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;


    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;


    //Caffeine核心接口：Cache
    // 子接口：LoadingCache(多个线程线程访问缓存时，若缓存没有对应的数据，只允许一个线程去数据库中取数据，其他线程则排队等候、
    // AsyncLoadingCache(异步缓存：支持并发处理

    private static final Logger logger= LoggerFactory.getLogger(DiscussPostService.class);

    @PostConstruct
    public void init(){
        //初始化帖子列表缓存
        postListCache= Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(String key) throws Exception {
                        if(key==null||key.length()==0){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        String[] params=key.split(":");
                        if(params==null||params.length!=2){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        int offset=Integer.valueOf(params[0]);
                        int limit=Integer.valueOf(params[1]);

                        //还可以加二级缓存：redis-->mysql

                        logger.debug("load posts from DB.");
                        return discussPostMapper.selectDiscussPost(0,offset,limit,1);
                    }
                });

        //初始化帖子总行数缓存
        postRowsCache=Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(Integer key) throws Exception {
                        logger.debug("load posts rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    //帖子列表缓存
    private LoadingCache<String,List<DiscussPost>> postListCache;

    //帖子总数缓存
    private LoadingCache<Integer,Integer> postRowsCache;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode) {
        if(userId==0&&orderMode==1){
            return postListCache.get(offset+":"+limit);
        }
        logger.debug("load posts from DB.");
        return discussPostMapper.selectDiscussPost(userId, offset, limit,orderMode);
    }

    public int findDiscussPostRows(int userId) {
        if(userId==0){
            return postRowsCache.get(userId);
        }
        logger.debug("load posts rows from DB.");
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


