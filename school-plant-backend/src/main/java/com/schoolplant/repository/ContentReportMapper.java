package com.schoolplant.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.entity.ContentReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ContentReportMapper extends BaseMapper<ContentReport> {

    @Select("SELECT * FROM content_reports WHERE status = #{status} ORDER BY created_at DESC")
    IPage<ContentReport> findByStatus(Page<?> page, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM content_reports WHERE post_id = #{postId} AND status = 'PENDING'")
    int countPendingByPostId(@Param("postId") Long postId);
}
