package com.light.community.dao.elasticsearch;

import com.light.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author light
 * @Description  实现elasticsearchRepository
 * @create 2023-05-24 15:09
 */

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {
}
