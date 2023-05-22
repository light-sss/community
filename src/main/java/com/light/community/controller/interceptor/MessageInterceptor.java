package com.light.community.controller.interceptor;

import com.light.community.entity.User;
import com.light.community.service.MessageService;
import com.light.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author light
 * @Description
 * @create 2023-05-22 14:15
 */

@Component
public class MessageInterceptor implements HandlerInterceptor {

	@Autowired
	private  HostHolder hostHolder;
	@Autowired
	private MessageService messageService;
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		User user = hostHolder.getUser();
		if(user!=null&&modelAndView!=null){
			int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
			int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
			modelAndView.addObject("allUnreadCount",letterUnreadCount+noticeUnreadCount);
		}
	}
}
