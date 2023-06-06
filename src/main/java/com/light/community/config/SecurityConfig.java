package com.light.community.config;

import com.light.community.util.CommunityConstant;
import com.light.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author light
 * @Description  springSecurity配置类
 * @create 2023-05-31 15:24
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {



	@Override
	public void configure(WebSecurity web) throws Exception {
		//忽略所有静态资源
		web.ignoring().antMatchers("/resources/**");
	}

	//授权相关配置（对现有路径权限配置
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers(
						"/comment/add/**",
						"/discuss/add",
						"/follow",
						"/unfollow",
						"/like",
						"/letter/**",
						"/user/setting",
						"/user/upload",
						"/notice/**"
				)
				.hasAnyAuthority(
						AUTHORITY_ADMIN,
						AUTHORITY_MODERATOR,
						AUTHORITY_USER
				)
				.antMatchers(
						"/discuss/top",
						"/discuss/wonderful"
				)
				.hasAnyAuthority(
						AUTHORITY_MODERATOR
				)
				.antMatchers(
						"/discuss/delete"
				)
				.hasAnyAuthority(
						AUTHORITY_ADMIN
				)
				.anyRequest().permitAll(); //除了这些请求以外任何请求都是允许访问的
				//.and().csrf().disable();   //不启用防止CSRF攻击


		//权限不够时的处理
		http.exceptionHandling()  //不同请求，返回结果不同（eg：同步、异步，因此用处理器更合适一点
				.authenticationEntryPoint(new AuthenticationEntryPoint() {
					@Override
					public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
						//没有登录时处理
						//考虑请求方式：如果普通请求，直接重定向到页面；如果异步请求，要返回json字符串
						//通过请求头判断请求方式是同步或异步
						String xRequestedWith = request.getHeader("x-requested-with");
						if("XMLHttpRequest".equals(xRequestedWith)){
							//异步请求
							response.setContentType("application/plain;charset=utf-8");  //响应字符串：返回响应数据类型：普通字符串（plain
							PrintWriter writer = response.getWriter();
							writer.write(CommunityUtil.getJsonString(403,"还未登陆！"));
						}else{

							//同步请求
							response.sendRedirect(request.getContextPath()+"/login");

						}


					}
				})
				.accessDeniedHandler(new AccessDeniedHandler() {
					@Override
					public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
						//登陆了但是权限不足时的处理
						//考虑请求方式：如果普通请求，直接重定向到页面；如果异步请求，要返回json字符串
						//通过请求头判断请求方式是同步或异步
						String xRequestedWith = request.getHeader("x-requested-with");
						if("XMLHttpRequest".equals(xRequestedWith)){
							//异步请求
							response.setContentType("application/plain;charset=utf-8");  //响应字符串：返回响应数据类型：普通字符串（plain
							PrintWriter writer = response.getWriter();
							writer.write(CommunityUtil.getJsonString(403,"你还没有访问此功能权限！"));
						}else{

							//同步请求
							response.sendRedirect(request.getContextPath()+"/denied");

						}

					}
				});


		//security底层默认拦截/logout（退出）请求,进行退出处理
		//因此要覆盖它的默认逻辑，才能执行自己的退出代码
		http.logout()
				.logoutUrl("/securitylogout");


		/**
		 * 关于认证：该项目为走security的认证方式，而是自己实现的认证方式，
		 * 在security中进项认证，实现将认证信息封装进UsernamePasswordAuthenticationToken（如果认证信息是账号密码）中
		 * security底层的filter会将token存到securityContext中，后面进项权限判断时，要从securityContext中取出认证过的授权信息
		 * 因此我们要绕过security底层认证逻辑，但是还要取到用户的权限信息，所以我们提供一个获取用户权限的方法，将用户权限在存入securityContex中
		 */
	}
}
