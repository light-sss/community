package com.light.community.util;

/**
 * @author light
 * @Description
 * @create 2023-03-25 15:55
 */
public interface CommunityConstant {
    //常量接口

    //1.激活成功
    int ACTIVATION_SUCCESS=0;

    //2.重复激活
    int ACTIVATION_REPEAT=1;

    //3.激活失败
    int ACTIVATION_FAILURE=2;

    //默认过期时间
    int  DEFAULT_EXPIRED_SECONDS=3600*12;

    //记住状态下的登录超时时间
    int REMEMBER_EXPIRED_SECONDS=3600*24*100;

    //实体类型：帖子（给帖子的评论
    int ENTITY_TYPE_POST=1;

    //评论类型：评论（评论的评论

    int ENTITY_TYPE_COMMENT=2;

    //实体类型：用户
    int ENTITY_TYPE_USER=3;

    //主题：评论
    String TOPIC_COMMENT="comment";
    //主题：点赞
    String TOPIC_LIKE="like";
    //主题：关注
    String TOPIC_FOLLOW="follow";

    //主题：发布
    String TOPIC_PUBLISH="publish";

    //系统用户ID
    int SYSTEM_USER_ID=1;

}
