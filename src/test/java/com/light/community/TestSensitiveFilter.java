package com.light.community;

import com.light.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author light
 * @Description
 * @create 2023-04-16 12:12
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)//希望启用此配置类
public class TestSensitiveFilter {
	@Autowired
	private SensitiveFilter sensitiveFilter;
	@Test
	public void testSensitive(){
		String text="这里可以玩耍，可以开派对哈哈哈！";
		text=sensitiveFilter.filter(text);
		System.out.println(text);
		text="这里可以☆玩☆耍☆，可以☆开☆派☆对☆，哈哈哈！";
		text=sensitiveFilter.filter(text);
		System.out.println(text);
	}
}
