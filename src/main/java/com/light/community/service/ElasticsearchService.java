package com.light.community.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author light
 * @Description
 * @create 2023-05-24 21:22
 */

@Service
public class ElasticsearchService {
	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Autowired
	private DiscussPostRepository discussPostRepository;

	//当新发布（修改）一条帖子时，要将帖子数据传入到elasticsearch中
	public void saveDiscussPost(DiscussPost discussPost){
		discussPostRepository.save(discussPost);
	}

	//当删除一条帖子时，需要将elasticsearch中内容同步删除
	public void deleteDiscussPost(int id){
		discussPostRepository.deleteById(id);
	}

	//实现搜索业务
	public Page<DiscussPost> searchDiscussPost(String keyword,int current,int limit){

		SearchQuery searchQuery=new NativeSearchQueryBuilder()  //构造NativeSearchQueryBuilder实现类
				.withQuery(QueryBuilders.multiMatchQuery(keyword,"title","content"))  //构建搜索条件
				.withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC)) //构造排序条件
				.withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
				.withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
				.withPageable(PageRequest.of(current,limit))  //构造分页条件：PageRequest.of(0,10)：当前页（从0开始 ，每页显示10条
				.withHighlightFields(
						//是否高亮显示：对搜索的关键词加标签
						new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
						new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
				).build();  //返回SearchQuery接口实现类

		return elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
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
	}

}
