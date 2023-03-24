package com.light.community;

import com.light.community.dao.DiscussPostMapper;
import com.light.community.dao.UserMapper;
import com.light.community.entity.DiscussPost;
import com.light.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

/**
 * @author light
 * @Description
 * @create 2023-03-22 20:34
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)//希望启用此配置类
public class MapperTests {
    @Autowired //将UserMapper注入
    private UserMapper userMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user=userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }


    @Test
    public void testInsertUser(){
        //要提前构建好一个User对象
        User user=new User();
        user.setUserName("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdate(){
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);
        rows= userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);
        rows=userMapper.updatePassword(150,"111111");
        System.out.println(rows);

    }

    @Autowired
    private DiscussPostMapper discussPostMapper;//注入依赖

    @Test
    public void testSelectPostMapper(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPost(149, 0, 10);

        for(DiscussPost i:discussPosts){
            System.out.println(i);
        }
        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }
}
