package com.light.community.controller;

import com.light.community.service.AlphaService;
import com.light.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author light
 * @Description
 * @create 2023-03-20 19:59
 */
//类上要有像Controller一样的注解才能被扫描
    /*
    和Controller功能一样的注解:
        Controller:处理请求的组件
        Service:开发的是业务组件最好用Service标明
        Component:在任何地方都能用
        Repository:开发的是数据库访问的组件
     */
@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){

        return "Hello Spring Boot";
    }

    @Autowired
    private AlphaService alphaService;
    @RequestMapping("/data")
    @ResponseBody
    public String getData(){//浏览器返回方法前提：这个方法必须有注解声明它的路径

        return alphaService.find();
    }

//在SpringMVC框架下如何获得请求对象，如何获得响应对象

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response){
         //获取请求数据
        System.out.println(request.getMethod());//获取请求方式
        System.out.println(request.getServletPath());//获取请求路径
        //Enumeration：迭代器 enumeration:迭代器地向
        Enumeration<String> enumeration =request.getHeaderNames();//得到所有的请求行的key（请求行是key-value结构）
        while(enumeration.hasMoreElements()){
            String name = enumeration.nextElement();//请求行的key
            String value = request.getHeader(name);//请求行的value
            System.out.println((name + ": " + value));
        }//消息头

        //请求体
        System.out.println(request.getParameter("code"));

        //返回响应数据
        response.setContentType("text/html;charset=utf-8");//响应返回数据类型（返回网页
        try(
                //编译时会自动加一个finally，将流关掉
                PrintWriter writer = response.getWriter();//获得输出流，
                ) {

            writer.write("<h1 style='color:yellow'>yuzuru hanyu</h1>");//向浏览器输出网页
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //处理浏览器请求分为两方面：接收请求的数据（基于request）和向浏览器返回相应数据（基于response）

    //GET请求：向服务器获取某些数据
    //查询所有学生：/student?current=1&limit=20  :当前第几页和最多显示几条数据
    @RequestMapping(path = "/student",method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            //可以通过@RequestParam这个注解对参数作更详尽的说明
            @RequestParam(name="current",required = false,defaultValue = "1") int current,
            @RequestParam(name="limit",required = false,defaultValue = "10")int limit){
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    //查询一个学生信息     /student/123:直接将信息编排到路径信息中
    @RequestMapping(path="/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(@PathVariable("id") int id){//@PathVariable路径变量
        System.out.println(id);
        return "a student";
    }

    //POST请求:浏览器向服务器提交数据
    //处理post请求
    @RequestMapping(path = "/student",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return "success";
    }


    //向浏览器返回响应
    //响应动态HTML数据
    @RequestMapping(path="/teacher",method = RequestMethod.GET)//声明访问路径，以何种方式请求
    //不加@ResponseBody注解默认返回HTML
    public ModelAndView getTeacher(){
        /*
        SpringMVC原理：
            所有组件由DispatchServlet调动，DispatchServlet会调用controller的某个方法
            这个方法会返回model和视图相关的数据，在将model和视图相关的数据提交给模板引擎
            模板引擎会进行渲染
         */
        //实例化ModelAndView对象
        ModelAndView mav=new ModelAndView();
        //动态传入数据(模板里需要多少个变量，就add多少个数据进去
        mav.addObject("name","Yuzuru Hanyu");
        mav.addObject("age","28");
        //需要设置对象模板的路径和名字
        mav.setViewName("/demo/view");//其实是view.html(需要创建模板
        //返回模板对象
        return mav;
    }

    @RequestMapping(path="/school",method = RequestMethod.GET)
    public String getSchool(Model model){
        model.addAttribute("name","西邮");
        model.addAttribute("age","75");

        return "/demo/view";   //return的是路径
    }

    //向浏览器json数据（异步请求：当前网页不刷新，但却访问服务器得到一个结果）
    //Java对象--->json字符串--->js对象
    @RequestMapping(path="/emp",method = RequestMethod.GET)
    @ResponseBody  //加上这个注解才可以返回json字符串
    public Map<String,Object> genEmp(){
        Map<String,Object> emp=new HashMap();
        emp.put("name","light");
        emp.put("age",21);
        emp.put("salary",1500);
        return emp;
        //加了@ResponseBody注解，并返回的是Map对象，DispatchServlet会将Map对象自动转换成json字符串，发送给浏览器
    }

    //返回一组数据
    @RequestMapping(path="/emps",method = RequestMethod.GET)
    @ResponseBody  //加上这个注解才可以返回json字符串
    public List<Map<String,Object>> genEmps(){
        List<Map<String,Object>> list=new ArrayList();
        Map<String,Object> emps=new HashMap();
        emps.put("name","light");
        emps.put("age",21);
        emps.put("salary",1500);
        list.add(emps);

        emps=new HashMap();
        emps.put("name","light1");
        emps.put("age",22);
        emps.put("salary",1600);
        list.add(emps);

        emps=new HashMap();
        emps.put("name","light2");
        emps.put("age",23);
        emps.put("salary",1700);
        list.add(emps);

        return list;
        //加了@ResponseBody注解，并返回的是Map对象，DispatchServlet会将Map对象自动转换成json字符串，发送给浏览器
    }


    //cookie示例：弥补http无状态
    @RequestMapping(path = "/cookie/set",method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        //将cookie存入response的头部中，响应时自动携带给浏览器

        //1.创建cookie
        Cookie cookie=new Cookie("code", CommunityUtil.generateUUID());

        //2.设置cookie生效范围
        cookie.setPath("/community/alpha");

        //3.设置cookie的生存时间
        cookie.setMaxAge(60*10);

        //4.发送cookie
        response.addCookie(cookie);

        return "set cookie";

    }

    @RequestMapping(path = "/cookie/get",method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code){
        System.out.println(code);
        return "get cookie";
    }


    //session:会自动创建
    @RequestMapping(path = "/session/set",method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session){
        session.setAttribute("id",1);
        session.setAttribute("name","light");
        return "set session";
    }
    @RequestMapping(path = "/session/get",method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session){
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "session get";
    }


}
