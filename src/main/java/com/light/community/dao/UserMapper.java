package com.light.community.dao;

import com.light.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author light
 * @Description
 * @create 2023-03-22 19:50
 */
//必须加一个注解才能让Spring容器装配bean
//@Repository:开发的是数据库访问的组件（这个注解也可以）
@Mapper //mybatis注解
public interface UserMapper {
    //操作：需要根据提供的方法，向配置文件中添加对应的SQL语句
    //查询用户
    User selectById(int id);//通过id查询
    User selectByName(String name);//通过name查询
    User selectByEmail(String email);//通过email查询

    //添加用户
    int insertUser(User user);//返回插入数据行数

    //修改用户状态:以 id为条件，更改状态
    int updateStatus(int id,int status);//返回修改条数

    //更新头像：以id为条件，更新头像路径
    int updateHeader(int id,String headerUrl);

    //更新密码:以id为条件，更新密码
    int updatePassword(int id,String password);

}
