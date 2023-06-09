package com.light.community.controller;

import com.light.community.entity.DiscussPost;
import com.light.community.entity.Page;
import com.light.community.service.ElasticsearchService;
import com.light.community.service.LikeService;
import com.light.community.service.MessageService;
import com.light.community.service.UserService;
import com.light.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author light
 * @Description
 * @create 2023-05-24 21:30
 */

@Controller
public class SearchController implements CommunityConstant {

	@Autowired
	private ElasticsearchService elasticsearchService;

	@Autowired
	private UserService userService;

	@Autowired
	private LikeService likeService;

	@RequestMapping(value = "/search",method = RequestMethod.GET)
	public String search(String keyword, Page page, Model model){
		//搜索帖子
		org.springframework.data.domain.Page<DiscussPost> searchResult =
				elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

		//聚合数据：帖子，帖子作者,点赞数，回复数
		List<Map<String,Object>> discussPosts=new ArrayList<>();
		if(searchResult!=null){
			for(DiscussPost post:searchResult){
				Map<String,Object> map=new HashMap<>();
				map.put("post",post);
				map.put("user", userService.findUserById(post.getUserId()));
				map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
				discussPosts.add(map);
			}
		}
		model.addAttribute("discussPosts",discussPosts);

		model.addAttribute("keyword",keyword);

		//设置分页
		page.setPath("/search?keyword="+keyword);
		page.setRows(searchResult==null? 0: (int) searchResult.getTotalElements());

		return "/site/search";
	}
}
