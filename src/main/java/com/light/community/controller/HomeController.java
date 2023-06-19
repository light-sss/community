package com.light.community.controller;

import com.light.community.dao.DiscussPostMapper;
import com.light.community.dao.UserMapper;
import com.light.community.entity.DiscussPost;
import com.light.community.entity.Page;
import com.light.community.entity.User;
import com.light.community.service.DiscussPostService;
import com.light.community.service.LikeService;
import com.light.community.service.UserService;
import com.light.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author light
 * @Description
 * @create 2023-03-23 16:49
 */
@Controller  //Controller访问路径可省略
public class HomeController implements CommunityConstant {
   //Controller会调用Service,需要将Service注入
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    //会通过discussPostService查到的userId查找具体User信息
    private UserService userService;

    @Autowired
    private LikeService likeService;


    //定义处理请求的方法
     //若返回的是一个HTML，则不用加@Response注解
    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page, @RequestParam(name = "orderMode",defaultValue = "0")int orderMode){
        //在SpringMVC中，方法参数都是由DispatcherServlet初始化的，
        //还会额外把Page对象装进Model中

        /*
        在方法调用前，SpringMVC会自动实例化Model和Page，并将Page注入Model中
        所以在thymeleaf模板中就可以直接访问Page对象中的数据
         */

        page.setRows(discussPostService.findDiscussPostRows(0));//设置总行数
        page.setPath("/index?orderMode="+orderMode);//设置返回路径


        //首先获取帖子信息
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        List<Map<String,Object>>  discussPosts=new ArrayList<>();
        for(DiscussPost post:list){
            Map<String,Object> map=new HashMap<>();
            map.put("post",post);
            User user = userService.findUserById(post.getUserId());//再通过获取的帖子信息的UserID找到User完整信息
            map.put("user",user);
            //获取点赞数量
            long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
            map.put("likeCount",likeCount);
            discussPosts.add(map);
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("orderMode",orderMode);

        //通过
        return "/index";
    }

    @RequestMapping(value = "/error",method = RequestMethod.GET)
    public String errorPage(){
        return "/error/500";
    }

    @RequestMapping(path = "/denied", method = {RequestMethod.GET, RequestMethod.GET})
    public String getDeniedPage() {
        return "/error/404";
    }
}

