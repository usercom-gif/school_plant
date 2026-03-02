package com.schoolplant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.entity.CareTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CareTaskMapper extends BaseMapper<CareTask> {

    @Select("SELECT * FROM care_tasks WHERE user_id = #{userId} AND status = #{status} ORDER BY due_date ASC")
    IPage<CareTask> findByUserAndStatus(Page<?> page, @Param("userId") Long userId, @Param("status") String status);

    @Select("SELECT * FROM care_tasks WHERE status = 'PENDING' AND due_date < #{date}")
    List<CareTask> findOverdueTasks(@Param("date") LocalDate date);

    @Select("SELECT COUNT(*) FROM care_tasks WHERE user_id = #{userId} AND status = 'COMPLETED'")
    int countCompletedByUser(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM care_tasks WHERE user_id = #{userId}")
    int countTotalByUser(@Param("userId") Long userId);
}
