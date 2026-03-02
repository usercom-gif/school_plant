package com.schoolplant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.schoolplant.entity.PlantAbnormality;
import org.springframework.web.multipart.MultipartFile;

public interface PlantAbnormalityService extends IService<PlantAbnormality> {

    /**
     * 上报异常
     * @param plantId 植物ID
     * @param reporterId 上报人ID
     * @param type 异常类型
     * @param desc 描述
     * @param images 图片文件
     * @return AI分析结果
     */
    String reportAbnormality(Long plantId, Long reporterId, String type, String desc, MultipartFile[] images);

    /**
     * 分派工单
     * @param abnormalityId 异常ID
     * @param maintainerId 养护员ID
     * @param operatorId 操作人ID
     */
    void assignMaintainer(Long abnormalityId, Long maintainerId, Long operatorId);

    /**
     * 处理异常
     * @param abnormalityId 异常ID
     * @param resolution 处理结果描述
     * @param materials 使用材料
     * @param evaluation 效果评估
     * @param images 处理后照片
     */
    void resolveAbnormality(Long abnormalityId, String resolution, String materials, String evaluation, MultipartFile[] images);

    /**
     * 统计养护员已处理的异常数量
     * @param maintainerId 养护员ID
     * @return 数量
     */
    int countResolvedByMaintainer(Long maintainerId);
}
