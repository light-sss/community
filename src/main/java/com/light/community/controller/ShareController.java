package com.light.community.controller;

import com.light.community.entity.Event;
import com.light.community.event.EventProducer;
import com.light.community.util.CommunityConstant;
import com.light.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author light
 * @Description
 * @create 2023-06-23 21:17
 */
@Controller
public class ShareController implements CommunityConstant {

	private static final Logger logger = LoggerFactory.getLogger(ShareController.class);


	@Autowired
	private EventProducer eventProducer;

	//域名
	@Value("${community.path.domain}")
	private String domain;

	//项目名
	@Value("${server.servlet.context-path}")
	private String contextPath;

	//存储路径
	@Value("${wk.image.storage}")
	private String wkImageStorage;

	@RequestMapping(value = "/share", method = RequestMethod.GET)
	@ResponseBody
	public String shareImage(String htmlUrl) {

		String fileName = CommunityUtil.generateUUID();  //文件名

		Event event = new Event()
				.setTopic(TOPIC_SHARE)
				.setData("htmlUrl", htmlUrl)  //请求路径
				.setData("fileName", fileName) //文件名
				.setData("suffix", ".png"); //文件后缀

		//异步方式：通过事件发送
		eventProducer.fireEvent(event);

		//返回访问路径
		Map<String, Object> map = new HashMap<>();
		map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);
		// eg: http://localhost:8080/community/share/image/33365834673863

		return CommunityUtil.getJsonString(0, null, map);
	}

	//获取长图(直接给浏览器返回一个图片，需要用response处理
	@RequestMapping(value = "/share/image/{fileName}", method = RequestMethod.GET)
	public void getImage(@PathVariable(name = "fileName") String fileName, HttpServletResponse response) {
		if (StringUtils.isBlank(fileName)) {
			throw new IllegalArgumentException("文件名不能为空！");
		}

		response.setContentType("image/png");

		//从本地读取文件
		File file = new File(wkImageStorage + "/" + fileName + ".png");

		try {
			OutputStream os = response.getOutputStream();
			FileInputStream is=new FileInputStream(file); //文件流：读取文件
			byte[] buffer=new byte[1024];
			int len=0;
			while((len=is.read(buffer))!=-1){
				os.write(buffer,0,len);
			}
		} catch (IOException e) {
			logger.error("获取长图失败："+e.getMessage());
		}


	}
}
