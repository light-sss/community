package com.light.community;

import com.light.community.dao.DiscussPostMapper;
import com.light.community.dao.elasticsearch.DiscussPostRepository;
import com.light.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author light
 * @Description Elasticsearch测试
 * @create 2023-05-24 15:10
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)//希望启用此配置类
public class ElasticsearchTests {

	@Autowired
	private DiscussPostMapper discussPostMapper;

	@Autowired
	private DiscussPostRepository discussPostRepository;

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;


	@Test
	public void testInsert(){
		discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
		discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
		discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
	}

	@Test
	public void testInsertList(){

		discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(0,0,1000,0));
	}


	@Test
	public void testUpdate(){
		/**
		 * 修改：将要修改的数据覆盖原先的
		 */

		DiscussPost post = discussPostMapper.selectDiscussPostById(231);
		post.setContent("我是新人，使劲灌水！");
		discussPostRepository.save(post);
	}

	@Test
	public void testDelete(){
		discussPostRepository.deleteById(231); //删除单条
		discussPostRepository.deleteAll();//删除所有数据
	}

	@Test
	public void testSearchByRepository(){
		//通过ElasticsearchRepository搜索
		SearchQuery searchQuery=new NativeSearchQueryBuilder()  //构造NativeSearchQueryBuilder实现类
				.withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))  //构建搜索条件
				.withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC)) //构造排序条件
				.withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
				.withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
				.withPageable(PageRequest.of(0,10))  //构造分页条件：PageRequest.of(0,10)：从第一页开始，每页显示10条
				.withHighlightFields(
						//是否高亮显示：对搜索的关键词加标签
						new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
						new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
				).build();  //返回SearchQuery接口实现类

		Page<DiscussPost> page = discussPostRepository.search(searchQuery);
		System.out.println(page.getTotalElements()); //总共元素数
		System.out.println(page.getTotalPages()); //总共页码数
		System.out.println(page.getNumber());  //当前页码数
		System.out.println(page.getSize()); //当前页有多少条数据
		for(DiscussPost post:page){
			System.out.println(post);
		}
	}

	/**
	 * 利用ElasticsearchRepository搜索时，再将搜索词显示为高亮时并未将其作为结果返回，
	 * 而是会将高亮数据作为另一份数据给到用户，用户需要将高亮数据整合到原始数据中心在显示出来
	 *
	 * ElasticsearchRepository底层调用：
	 * 		elasticTemplate.queryForPage(searchQuery,class,searchResultMapper)
	 * 查询的高亮数据会由searchResultMapper进行处理
	 *
	 * 但ElasticsearchRepository底层并为实现这个功能，他只获取到了值，但并未返回
	 *
	 * 直接用ElasticTemplate方法进行搜索
	 */

	@Test
	public void testSearchByTemplate(){

		SearchQuery searchQuery=new NativeSearchQueryBuilder()  //构造NativeSearchQueryBuilder实现类
				.withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))  //构建搜索条件
				.withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC)) //构造排序条件
				.withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
				.withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
				.withPageable(PageRequest.of(0,10))  //构造分页条件：PageRequest.of(0,10)：当前页（从0开始 ，每页显示10条
				.withHighlightFields(
						//是否高亮显示：对搜索的关键词加标签
						new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
						new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
				).build();  //返回SearchQuery接口实现类

		Page<DiscussPost> page = elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
			@Override
			public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
				//处理
				SearchHits hits = searchResponse.getHits(); //获取搜索的结果数据
				if(hits.totalHits<0){
					return null;  //未获取到结果直接返回
				}

				//将获取到的结果装进list中
				List<DiscussPost> list = new ArrayList<>();
				//遍历命中数据
				for(SearchHit hit:hits){
					//将命中数据吧包装到实体类中
					DiscussPost post=new DiscussPost();
					//数据形式是以json形式存在的，并将json数据封装为了map格式
					//因此将数据以map形式取出--->转为string再存入DiscussPost对象中
					String id = hit.getSourceAsMap().get("id").toString();
					post.setId(Integer.valueOf(id));
					String userId = hit.getSourceAsMap().get("userId").toString();
					post.setUserId(Integer.valueOf(userId));
					String title = hit.getSourceAsMap().get("title").toString();//原始的title
					post.setTitle(title);
					String content = hit.getSourceAsMap().get("content").toString();
					post.setContent(content);
					String status = hit.getSourceAsMap().get("status").toString();
					post.setStatus(Integer.valueOf(status));
					String createTime = hit.getSourceAsMap().get("createTime").toString();
					post.setCreateTime(new Date(Long.valueOf(createTime)));
					String commentCount = hit.getSourceAsMap().get("commentCount").toString();
					post.setCommentCount(Integer.valueOf(commentCount));

					//处理显示高亮结果
					HighlightField titleField = hit.getHighlightFields().get("title");
					if(titleField!=null){
						//获取到高亮结果，将高亮结果对原内容进行替换
						post.setTitle(titleField.getFragments()[0].toString());
					}

					HighlightField contentField = hit.getHighlightFields().get("content");
					if(contentField!=null){
						//获取到高亮结果，将高亮结果对原内容进行替换
						post.setContent(contentField.getFragments()[0].toString());
					}

					list.add(post);

				}
				return new AggregatedPageImpl(list,pageable, hits.totalHits
						,searchResponse.getAggregations(),searchResponse.getScrollId(),hits.getMaxScore());
			}
		});

		System.out.println(page.getTotalElements()); //总共元素数
		System.out.println(page.getTotalPages()); //总共页码数
		System.out.println(page.getNumber());  //当前页码数
		System.out.println(page.getSize()); //当前页有多少条数据
		for(DiscussPost post:page){
			System.out.println(post);
		}

	}

}
