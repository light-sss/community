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

}
