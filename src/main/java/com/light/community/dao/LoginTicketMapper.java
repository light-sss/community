package com.light.community.dao;

import com.light.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * @author light
 * @Description 对登录凭证实体类的操作
 * @create 2023-03-27 21:43
 */
@Mapper
@Deprecated
public interface LoginTicketMapper {
    //通过注解执行SQL语句
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired});"
    })
    @Options(useGeneratedKeys = true,keyProperty="id")//设置id值自增
    //插入数据：返回影响行数
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket ",
            "where ticket=#{ticket};"
    })
    //根据ticket查找对应登录凭证信息:返回对应对象信息
     LoginTicket selectByTicket(String ticket);

    @Update({
            "update login_ticket set status=#{status} ",
            "where ticket=#{ticket};"
    })
    //更改status状态（根据ticket值
    int updateStatus(String ticket,int status);
}
