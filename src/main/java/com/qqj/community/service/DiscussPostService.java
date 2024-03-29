package com.qqj.community.service;

import com.qqj.community.dao.DiscussPostMapper;
import com.qqj.community.entity.DiscussPost;
import com.qqj.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode){
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }


    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post){
        if (post ==null ){
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        return discussPostMapper.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    public int updateType(int id, int type){
        return discussPostMapper.updateType(id, type);
    }
    public int updateStatus(int id, int type){
        return discussPostMapper.updateStatues(id, type);
    }
    public int updateScore(int id, double score){
        return discussPostMapper.updateScore(id, score);
    }

}
