package com.light.community.util;

import com.light.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @author light
 * @Description 持有用户信息，用于代替Session对象
 * @create 2023-04-05 20:40
 */
@Component
public class HostHolder {
    //根据线程值获取当前用户信息（map
    private ThreadLocal<User> users=new ThreadLocal<User>();

    //存值用户
    public void setUser(User user){
        users.set(user);
    }

    //获取用户
    public User getUser(){
        return users.get();
    }

    //清除
    public void clear(){
        users.remove();
    }
}
