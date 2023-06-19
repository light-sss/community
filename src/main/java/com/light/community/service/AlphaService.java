package com.light.community.service;

import com.light.community.dao.AlphaDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
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
    public static final Logger logger= LoggerFactory.getLogger(AlphaService.class);
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

    //让该方法在多线程环境下，被异步调用，
    @Async
    public void execute1(){
        logger.debug("execute1");
    }

    /**
     * @Scheduled(initialDelay【延迟多久开始执行】,fixedRate【时间间隔】)
     */
    //@Scheduled(initialDelay = 1000,fixedRate = 1000)  //定时任务
    //public void execute2(){
    //    logger.debug("execute2");
    //}



}
