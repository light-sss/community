package com.light.community.dao;

import org.springframework.stereotype.Repository;

/**
 * @author light
 * @Description
 * @create 2023-03-21 16:46
 */
@Repository("alphaDaoHibernate")  //访问数据库;定义bean的名字，直接加@Repository("alphaDaoHibernate")
//定义了名字可以通过容器强制获取这个bean
public class AlphaDaoHibernate implements AlphaDao{


    @Override
    public String select() {
        return "Hibernate";
    }
}
