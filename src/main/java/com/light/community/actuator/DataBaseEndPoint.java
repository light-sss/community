package com.light.community.actuator;

import com.light.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author light
 * @Description 自定义监控端点：
 * @create 2023-07-18 11:46
 */
@Component
@Endpoint(id = "database")  //表明是一个端点
public class DataBaseEndPoint {

	private static final Logger logger= LoggerFactory.getLogger(DataBaseEndPoint.class);
	@Autowired
	private DataSource dataSource;

	@ReadOperation //表示get请求
	public String dataBaseConnection(){
		try (
				Connection connection = dataSource.getConnection();
		){
			return CommunityUtil.getJsonString(0,"数据库连接成功！");
		} catch (SQLException e) {

			logger.error("数据库连接失败！"+e);
			return CommunityUtil.getJsonString(1,"数据库连接失败!");

		}

	}


}
