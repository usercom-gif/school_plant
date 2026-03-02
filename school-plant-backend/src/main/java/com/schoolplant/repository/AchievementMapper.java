package com.schoolplant.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.entity.Achievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AchievementMapper extends BaseMapper<Achievement> {

    @Select("SELECT * FROM achievements WHERE user_id = #{userId} AND semester = #{semester}")
    Achievement findByUserAndSemester(@Param("userId") Long userId, @Param("semester") String semester);

    @Select("SELECT * FROM achievements WHERE semester = #{semester} AND is_outstanding = 1 ORDER BY task_completion_rate DESC")
    IPage<Achievement> findOutstandingBySemester(Page<?> page, @Param("semester") String semester);
}
