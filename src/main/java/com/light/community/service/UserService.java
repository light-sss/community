package com.light.community.service;

import com.light.community.dao.LoginTicketMapper;
import com.light.community.dao.UserMapper;
import com.light.community.entity.LoginTicket;
import com.light.community.entity.User;
import com.light.community.util.CommunityConstant;
import com.light.community.util.CommunityUtil;
import com.light.community.util.MailClient;
import com.light.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author light
 * @Description 查询用户相关信息
 * @create 2023-03-23 16:42
 */
@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;
    //账号注册过程中需要发邮件：需将发邮件的功能注入
    @Autowired
    private MailClient mailClient;

    //注入模板引擎
    @Autowired
    private TemplateEngine templateEngine;


    //注册账号需要激活码，激活码中包含域名和项目名，所以需要将配置文件中的域名和项目名都注入
    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    //注入验证登录凭证等操作
    //@Autowired
    //private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    public User findUserById(int id){

        // 之前：return userMapper.selectById(id);
        User user = getCache(id);  //先从缓存中查
        if(user==null){
            user = initCache(id); //缓存中没有
        }
        return user;
    }

    //编写注册业务
    public Map<String,Object> register(User user){
        Map<String,Object> map=new HashMap<>();
        //对参数进行判断
        //1.首先对空值进行判断：对象不能为空
        if(user==null){
            throw new RuntimeException("参数不能为空");
        }
        //2.对象账号不能为空
        if(StringUtils.isBlank(user.getUserName())){
            map.put("userNameMessage","账号不能为空");
            return map;
        }
        //3.对象密码不能为空
        if(StringUtils.isBlank(user.getPassword())){
            map.put("userPasswordMessage","密码不能为空");
            return map;
        }
        //4.对象邮箱不能为空
        if(StringUtils.isBlank(user.getEmail())){
            map.put("userEmailMessage","邮箱不能为空");
            return map;
        }
        //做注册处理：首先判断注册账号、邮箱是否已存在
        //验证账号
        User u = userMapper.selectByName(user.getUserName());
        if(u!=null){
            map.put("userNameMessage","该账号已存在");
            return map;
        }
        //验证邮箱：通过邮箱去判断用户是否存在
        u= userMapper.selectByEmail(user.getEmail());
        if(u!=null){
            map.put("userEmailMessage","邮箱已被注册");
            return map;
        }

        //注册用户
        //1.对密码加密
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5((user.getPassword())+user.getSalt()));
        //2.用户类型：普通用户（type=0），状态：未激活（status=0）
        user.setType(0);
        user.setStatus(0);
        //3.激活用户：需要激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        //4.赋给用户随机头像
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        //注册时间
        user.setCreateTime(new Date());
        //将user对象添加到库里
        userMapper.insertUser(user);//会自动生成id
        //激活邮件
        Context context=new Context();
        context.setVariable("email",user.getEmail());
        //规定激活路径URL  ：http://localhost:8080/community/activation/101/code
        String url=domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        //利用模板引擎生成邮件内容
        String content=templateEngine.process("/mail/activation",context);
        //发送邮件
        mailClient.sendMail(user.getEmail(),"激活账号",content);

        return map;
    }

    //编写激活业务
    public int activation(int userId,String code){
        //首先找到用户，在判断激活码是否合法
        User user=userMapper.selectById(userId);
        if(user.getStatus()==1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){ //激活码和传入的激活码一样
            //更改用户状态
            userMapper.updateStatus(userId,1);
            //用户信息更改，清除缓存
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE; //激活码不等
        }
    }

    //用户登录验证（用户登录凭证
    public Map<String,Object> login(String userName,String password,int expiredSeconds){
        Map<String,Object> map=new HashMap<>();//将验证信息封装进map中
        //账号不能为空
        if(StringUtils.isBlank(userName)){
            map.put("userNameMsg","账号不能为空！");
            return map;
        }

        //密码不能为空
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }

        User user = userMapper.selectByName(userName);
        //账号验证
        if(user==null){
            map.put("userNameMsg","账号不存在！");
            return map;
        }
        //账号是否激活
        if(user.getStatus()==0){
            map.put("userNameMsg","账号未激活！");
            return map;
        }
        //密码验证
        password=CommunityUtil.md5(password + user.getSalt());
        if(!password.equals(user.getPassword())){
            map.put("passwordMsg","密码错误！");
            return map;
        }

        //验证通过，生成用户凭证
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());//生成随机字符串
        loginTicket.setStatus(0);//账号登录状态0：未过期
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
        //原来：loginTicketMapper.insertLoginTicket(loginTicket);//存入用户登录凭证
        //现在
        //生成key
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        //将登录凭证存入redis
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
        map.put("ticket",loginTicket.getTicket());//将ticket存入，方便传给浏览器
        return map;
    }


    public void logout(String ticket){

        //原来：loginTicketMapper.updateStatus(ticket,1);

        //现在：更改redis中登录凭证状态 ：先取、在更改、再存入
        //生成key
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);

    }

    //获取登录凭证ticket
    public LoginTicket findLoginTicket(String ticket){

        //原来：return loginTicketMapper.selectByTicket(ticket);

        //现在
        //生成key
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    //更改头像信息（headerUrL
    public int updateHeaderUrl(int userId,String headerUrl){

        //之前：return userMapper.updateHeader(userId,headerUrl);

        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    //修改密码
    public int updatePassword(int userId,String password){

        //之前：return userMapper.updatePassword(userId,password);
        int rows = userMapper.updatePassword(userId, password);
        clearCache(userId);
        return rows;
    }

    //根据用户姓名查询用户
    public User selectUserByName(String userName){
        return userMapper.selectByName(userName);
    }


    //1、优先从缓存中取值
    public User getCache(int userId){
        //构建用户key
        String userKey=RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);

    }

    //2、若缓存中无值，则从mysql中查找,后在写入缓存中
    public User initCache(int userId){
        User user = userMapper.selectById(userId);
        //构建用户key
        String userKey=RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    //3、当用户信息发生更改时清除缓存
    public void clearCache(int userId){
        //构建用户key
        String userKey=RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    //获取用户权限
    public Collection<? extends GrantedAuthority> getAuthority(int userId){
        User user = this.findUserById(userId);
        List<GrantedAuthority> list=new ArrayList<>(); //将用户权限信息存入list中
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }


}
