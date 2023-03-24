package com.light.community.service;

import com.light.community.dao.UserMapper;
import com.light.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author light
 * @Description 查询用户相关信息
 * @create 2023-03-23 16:42
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }
}
