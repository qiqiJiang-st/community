package com.qqj.community.dao;

import com.qqj.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //这里用到动态sql
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);
    //如果只有一个参数，并且在<if>中使用，则要加Param注解，它用于给参数取别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);
}
