package com.qqj.community;

import com.alibaba.fastjson.JSONObject;
import com.qqj.community.dao.DiscussPostMapper;
import com.qqj.community.dao.elasticsearch.DiscussPostRepository;
import com.qqj.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper discussMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ElasticsearchRestTemplate elasticSearchRestTemplate;

    @Test
    public void testInsert(){
        discussRepository.save(discussMapper.selectDiscussPostById(241));
        discussRepository.save(discussMapper.selectDiscussPostById(242));
        discussRepository.save(discussMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList(){
        discussRepository.saveAll(discussMapper.selectDiscussPosts(101,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(102,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(103,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(111,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(112,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(131,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(132,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(133,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(134,0,100));

    }

    @Test
    public void testUpdate(){
        DiscussPost post = discussMapper.selectDiscussPostById(231);
        post.setContent("我是新人，使劲灌水。");
        discussRepository.save(post);

    }

    @Test
    public void testDelete(){
        discussRepository.deleteById(231);
//        discussRepository.deleteAll();
    }

    @Test
    public void testSearchByRestTemplate() throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<em>");
        highlightBuilder.postTags("</em>");

        //构建搜索条件

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0)//指定从哪条开始查询
                .size(10)//需要查出的总记录数
                .highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse =  restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits()){
            DiscussPost post= JSONObject.parseObject(hit.getSourceAsString(),DiscussPost.class);

            //处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if(titleField != null){
                post.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if(titleField != null){
                post.setTitle(titleField.getFragments()[0].toString());
            }
            System.out.println(post);

            list.add(post);

        }

//        PageImpl<DiscussPost> pageInfo = new PageImpl<DiscussPost>()

    }

    @Test
    public void testSearch(){
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<em>");
        highlightBuilder.postTags("</em>");
        //构建搜索条件
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightBuilder(highlightBuilder)
                .build();
        SearchHits<DiscussPost> search = elasticSearchRestTemplate.search(build,DiscussPost.class);
        SearchPage<DiscussPost> page = SearchHitSupport.searchPageFor(search,build.getPageable());
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (org.springframework.data.elasticsearch.core.SearchHit<DiscussPost> discussPostSearchHit : page) {
            System.out.println(discussPostSearchHit.getHighlightFields()); //高亮内容
            System.out.println(discussPostSearchHit.getContent()); //原始内容
        }

        //下面是封装将高亮部分成一个page对象，也可以不做，直接discussPostSearchHit.getHightlightFields()获取
        List<DiscussPost> list = new ArrayList<>();
        for (org.springframework.data.elasticsearch.core.SearchHit<DiscussPost> discussPostSearchHit:page){
            DiscussPost discussPost = discussPostSearchHit.getContent();
            if(discussPostSearchHit.getHighlightFields().get("title")!=null){
                discussPost.setTitle(discussPostSearchHit.getHighlightFields().get("title").get(0));
            }
            if(discussPostSearchHit.getHighlightFields().get("content")!=null){
                discussPost.setTitle(discussPostSearchHit.getHighlightFields().get("content").get(0));
            }
            list.add(discussPost);
        }

        PageImpl<DiscussPost> pageInfo = new PageImpl<DiscussPost>(list,build.getPageable(),search.getTotalHits());
        System.out.println(pageInfo.getTotalElements());
        System.out.println(pageInfo.getTotalPages());
        System.out.println(pageInfo.getNumber());
        System.out.println(pageInfo.getSize());
        for (DiscussPost post:pageInfo){
            System.out.println(post);
        }

    }
}
