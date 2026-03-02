package com.schoolplant.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.entity.AdoptionApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdoptionApplicationMapper extends BaseMapper<AdoptionApplication> {

    @Select("SELECT * FROM adoption_applications WHERE user_id = #{userId} AND status = #{status}")
    List<AdoptionApplication> findByUserAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Select("SELECT * FROM adoption_applications WHERE plant_id = #{plantId} AND status = 'APPROVED'")
    AdoptionApplication findApprovedByPlantId(@Param("plantId") Long plantId);

    @Select("SELECT COUNT(*) FROM adoption_applications WHERE user_id = #{userId} AND status = 'PENDING'")
    int countPendingByUser(@Param("userId") Long userId);
}
