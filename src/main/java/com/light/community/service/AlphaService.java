package com.light.community.service;

import com.light.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author light
 * @Description
 * @create 2023-03-21 17:04
 */

@Service  //开发的是业务组件
public class AlphaService {
    public AlphaService(){
        System.out.println("实例化化AlphaService");
    }

    @PostConstruct   //会在合适的时间调用此方法：这里是在调用构造器后调用此方法
    public void init(){
        System.out.println("初始化AlphaService");
    }

    @PreDestroy  //在销毁对象之前调用
    public void destory(){
        System.out.println("销毁AlphaService");
    }

    @Autowired
    private  AlphaDao alphaDao;

    public String find(){
        return alphaDao.select();
    }

}
