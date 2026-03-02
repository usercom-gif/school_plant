package com.schoolplant.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.entity.PlantAbnormality;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlantAbnormalityMapper extends BaseMapper<PlantAbnormality> {

    @Select("SELECT * FROM plant_abnormalities WHERE status = #{status} ORDER BY created_at DESC")
    IPage<PlantAbnormality> findByStatus(Page<?> page, @Param("status") String status);

    @Select("SELECT * FROM plant_abnormalities WHERE reporter_id = #{userId}")
    List<PlantAbnormality> findByReporterId(@Param("userId") Long userId);

    @Select("SELECT * FROM plant_abnormalities WHERE maintainer_id = #{maintainerId} AND status IN ('ASSIGNED', 'REPORTED')")
    List<PlantAbnormality> findAssignedToMaintainer(@Param("maintainerId") Long maintainerId);
}
