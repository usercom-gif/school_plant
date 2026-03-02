package com.schoolplant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.schoolplant.entity.AdoptionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdoptionRecordMapper extends BaseMapper<AdoptionRecord> {
    
    @Select("SELECT COUNT(*) FROM adoption_records WHERE user_id = #{userId} AND status = 'ACTIVE'")
    int countActiveByUser(@Param("userId") Long userId);
}
