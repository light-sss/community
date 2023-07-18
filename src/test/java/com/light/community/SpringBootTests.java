package com.light.community;

import com.light.community.entity.DiscussPost;
import com.light.community.service.DiscussPostService;
import org.apache.http.util.Asserts;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.Date;

/**
 * @author light
 * @Description
 * @create 2023-07-18 9:43
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)//希望启用此配置类
public class SpringBootTests {

	@Autowired
	DiscussPostService discussPostService;

	private DiscussPost data=new DiscussPost();

	@BeforeClass
	public static void BeforeClass(){
		System.out.println("BeforeClass");
	}

	@AfterClass
	public static void AfterClass(){
		System.out.println("AfterClass");
	}

	@Before
	public void before(){
		System.out.println("before");

		//初始化data数据
		data.setUserId(111);
		data.setTitle("testTitle");
		data.setContent("testContent");
		data.setCreateTime(new Date());
		discussPostService.addDiscussPost(data);
	}

	@After
	public void After(){
		System.out.println("After");

		//销毁data数据
		discussPostService.updateStatus(data.getId(),2);
	}

	@Test
	public void test1(){
		System.out.println("test1");
	}
	@Test
	public void test2(){
		System.out.println("test2");
	}
	@Test
	public void test3(){
		System.out.println("test3");
	}

	@Test
	public void findByIdTest(){
		DiscussPost post = discussPostService.findDiscussPostById(data.getId());
		//断言
		Assert.assertNotNull(post);
		Assert.assertEquals(data.getTitle(),post.getTitle());
		Assert.assertEquals(data.getContent(),post.getContent());
	}

	@Test
	public void updateScoreTest(){
		int rows = discussPostService.updateScore(data.getId(), 2000.00);
		Assert.assertEquals(1,rows);
		DiscussPost post = discussPostService.findDiscussPostById(data.getId());
		Assert.assertEquals(2000,post.getScore(),2);

	}
}
