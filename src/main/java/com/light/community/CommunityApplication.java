package com.light.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

//注解所标识的类是一个配置文件
@SpringBootApplication   //标注这是一个spring boot应用
public class CommunityApplication {

	@PostConstruct
	public void init(){
		//解决netty启动冲突问题
		//see Netty4Utils.setAvailableProcessors
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {

		//自动创建Spring容器，向下扫描，将bean装配到容器中
		//会扫描配置类所在的包以及子包下的类
		SpringApplication.run(CommunityApplication.class, args);

	}

}
