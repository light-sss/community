package com.light.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @author light
 * @Description
 * @create 2023-06-23 20:43
 */
@Configuration
public class WkConfig {
	//在服务启动时生成图片目录
	private final static Logger logger= LoggerFactory.getLogger(WkConfig.class);

	@Value("${wk.image.storage}")
	private String wkImageStorage;
	@PostConstruct
	public void init(){

		//创建wk图片目录
		File file=new File(wkImageStorage);
		if(!file.exists()){
			file.mkdir();
			logger.info("创建wk图片目录："+wkImageStorage);
		}
	}
}
