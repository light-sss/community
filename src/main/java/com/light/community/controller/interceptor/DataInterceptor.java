package com.light.community.controller.interceptor;

import com.light.community.entity.User;
import com.light.community.service.DataService;
import com.light.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author light
 * @Description
 * @create 2023-06-11 21:28
 */
@Component
public class DataInterceptor implements HandlerInterceptor {
	@Autowired
	private DataService dataService;

	@Autowired
	private HostHolder hostHolder;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		//统计uv
		String ip=request.getRemoteUser();
		dataService.recordUV(ip);

		//统计dau
		User user = hostHolder.getUser();
		if(user!=null){
			int userId = user.getId();
			dataService.recordDAU(userId);
		}
		return true;
	}
}
