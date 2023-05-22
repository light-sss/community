package com.light.community.controller;

import com.light.community.annotation.LoginRequired;
import com.light.community.entity.User;
import com.light.community.service.FollowService;
import com.light.community.service.LikeService;
import com.light.community.service.UserService;
import com.light.community.util.CommunityConstant;
import com.light.community.util.CommunityUtil;
import com.light.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author light
 * @Description
 * @create 2023-04-11 21:30
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    private  static final Logger logger= LoggerFactory.getLogger(UserController.class);



    //跳转到账号设置页面
    @LoginRequired //需要拦截请求：安全
    @RequestMapping(value = "/setting",method = RequestMethod.GET)
    public String getSettings(){

        return "/site/setting";
    }

    //将头像上传的头像保存到本地
    @LoginRequired  //需要拦截请求：安全
    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        //判断获取的头像是否存在
        if(headerImage==null){
            model.addAttribute("error","头像不能为空!");
            return "/site/setting";
        }
        //头像存在
        //为头像保存一个随机名，以免重复
        String fileName = headerImage.getOriginalFilename(); //获取头像名
        String suffix = fileName.substring(fileName.lastIndexOf("."));//获取后缀：.xxx
        //判断头像格式是否正确
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","头像格式不正确!");
            return "/site/setting";
        }

        //设置新的文件名：生成随机文件名
        fileName = CommunityUtil.generateUUID() + fileName;
        //确定文件存放路径:硬盘文件路径+文件名（注入配置的硬盘路径
        File dest = new File(uploadPath+"/"+ fileName);
        //将头像信息写入到文件中
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("文件上传失败："+e.getMessage()); //打印异常信息
            throw new RuntimeException("上传失败，服务器发生异常！",e);
        }
        //更新当前头像路径（web路径）
        //http://localhost:8080/community/user/header/xxx.png
        String headerUrl=domain+contextPath+"/user/header/"+fileName;
        //获取当前对象
        User user = hostHolder.getUser();
        userService.updateHeaderUrl(user.getId(),headerUrl);

        //头像设置完成，页面重定向至首页
        return "redirect:/index";
    }

    //从本地获取头像
    @RequestMapping(value = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //获取服务器存放文件路径(硬盘的
        fileName=uploadPath+"/"+fileName;
        //获取文件后缀
        String suffix=fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix); //获取文件类型
        //获取输入输出流，将文件从本地读入并输出到浏览器上
        try(
                //java7语法：会自动关闭流
                FileInputStream is=new FileInputStream(fileName);//获取输入流将文件从本地读入程序
                OutputStream os = response.getOutputStream();//获取输出流，将文件响应到浏览器上
                ) {
            byte[] buffer=new byte[1024];
            int len=0;
            while((len=is.read(buffer))!=-1){
                os.write(buffer,0,len);
            }
        } catch (IOException e) {
            logger.error("读取头像失败："+e.getMessage());
        }

    }

    //修改密码
    @RequestMapping(value = "/updatePassword",method = RequestMethod.POST)//@CookieValue("ticket") String ticket：从浏览器器中得到cookie
    public String updatePassword(String oldPassword, String newPassword, String confirmNewPassword, Model model,@CookieValue("ticket") String ticket){
        //对传入参数进行判断，不能为空
        if(StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldPasswordMsg","请输入原始密码！");
            return "site/setting";
        }
        if(StringUtils.isBlank(newPassword)){
            model.addAttribute("newPasswordMsg","请输入新密码！");
            return "site/setting";
        }
        if(StringUtils.isBlank(confirmNewPassword)){
            model.addAttribute("confirmNewPasswordMsg","请输入确认密码！");
            return "site/setting";
        }
        //首先通过所持有的的用户对象获取当前用户
        User user = hostHolder.getUser();
        //判断用户输入的原密码是否与存储的原密码一致
        //首先对用户输入的原密码进行加密处理
         oldPassword = CommunityUtil.md5(oldPassword+user.getSalt());
         if(!oldPassword.equals(user.getPassword())){
             model.addAttribute("oldPasswordMsg","该密码与原密码不符!");
             return "site/setting";
         }
         //判断新输入密码与原密码是否一致
        //对新密码进行加密
        newPassword=CommunityUtil.md5(newPassword+user.getSalt());
        if(newPassword.equals(user.getPassword())){//判断
            model.addAttribute("newPasswordMsg","新密码与原密码一致!");
            return "site/setting";
        }
        //对确认密码进行加密
        confirmNewPassword=CommunityUtil.md5(confirmNewPassword+user.getSalt());
        if(!newPassword.equals(confirmNewPassword)){//判断
            model.addAttribute("confirmNewPasswordMsg","两次密码不一致!");
            return "site/setting";
        }
        userService.updatePassword(user.getId(),newPassword);
        //修改密码后，用户需要重新登陆，所以在本次持有中释放用户
        userService.logout(ticket);
        return "redirect:/login";
    }


    //查看个人主页:不仅查看自己的主页，还能查看别人的主页
    //userId:要查看主页的人的用户id
    @RequestMapping(value = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);
        //用户获赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //用户关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //用户粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        //登录用户是否已关注该用户
        boolean hasFollowed=false;
        if(hostHolder.getUser()!=null){

            hasFollowed=followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }

}
