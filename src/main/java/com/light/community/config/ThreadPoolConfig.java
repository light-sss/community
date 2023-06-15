package com.light.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;

/**
 * @author light
 * @Description 如果需要ThreadPoolTaskScheduler线程池生效，需要进行相关配置
 * @create 2023-06-15 20:00
 */

@Configuration
@EnableScheduling  //启用定时任务（默认不启用
@EnableAsync
public class ThreadPoolConfig {
}
