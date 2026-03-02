package com.schoolplant.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.entity.Plant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlantMapper extends BaseMapper<Plant> {

    @Select("SELECT * FROM plants WHERE status = #{status} AND region = #{region}")
    IPage<Plant> findByStatusAndRegion(Page<?> page, @Param("status") String status, @Param("region") String region);

    @Select("SELECT * FROM plants WHERE name LIKE CONCAT('%', #{keyword}, '%') OR species LIKE CONCAT('%', #{keyword}, '%')")
    IPage<Plant> search(Page<?> page, @Param("keyword") String keyword);

    @Select("SELECT * FROM plants WHERE plant_code = #{code}")
    Plant findByCode(@Param("code") String code);

    @Select("SELECT DISTINCT species FROM plants")
    List<String> findAllSpecies();
}
