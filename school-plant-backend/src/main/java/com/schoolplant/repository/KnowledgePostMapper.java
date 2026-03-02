package com.schoolplant.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.entity.KnowledgePost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface KnowledgePostMapper extends BaseMapper<KnowledgePost> {

    @Select("SELECT * FROM knowledge_posts WHERE status = 'ACTIVE' ORDER BY is_featured DESC, created_at DESC")
    IPage<KnowledgePost> findActivePosts(Page<?> page);

    @Select("SELECT * FROM knowledge_posts WHERE status = 'ACTIVE' AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%'))")
    IPage<KnowledgePost> search(Page<?> page, @Param("keyword") String keyword);

    @Select("SELECT * FROM knowledge_posts WHERE status = 'ACTIVE' AND is_featured = 1")
    List<KnowledgePost> findFeaturedPosts();

    @Update("UPDATE knowledge_posts SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id);
}
