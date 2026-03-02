package com.schoolplant.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.schoolplant.entity.AbnormalityProcessLog;
import com.schoolplant.entity.PlantAbnormality;
import com.schoolplant.mapper.AbnormalityProcessLogMapper;
import com.schoolplant.mapper.PlantAbnormalityMapper;
import com.schoolplant.service.DifyService;
import com.schoolplant.service.PlantAbnormalityService;
import com.schoolplant.websocket.AbnormalityWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PlantAbnormalityServiceImpl extends ServiceImpl<PlantAbnormalityMapper, PlantAbnormality> implements PlantAbnormalityService {

    @Autowired
    private DifyService difyService;

    @Autowired
    private AbnormalityProcessLogMapper processLogMapper;

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String reportAbnormality(Long plantId, Long reporterId, String type, String desc, MultipartFile[] images) {
        // 1. Save Images
        List<String> imageUrls = new ArrayList<>();
        File firstImageFile = null;

        try {
            Files.createDirectories(Paths.get(uploadPath));
            for (int i = 0; i < images.length; i++) {
                MultipartFile img = images[i];
                String fileName = UUID.randomUUID() + "_" + img.getOriginalFilename();
                File dest = new File(uploadPath, fileName);
                img.transferTo(dest);
                imageUrls.add("/uploads/" + fileName); // Assuming static resource mapping
                if (i == 0) firstImageFile = dest;
            }
        } catch (IOException e) {
            throw new RuntimeException("图片上传失败", e);
        }

        // 2. Call AI Analysis
        String aiSuggestion = "暂无AI建议";
        if (firstImageFile != null) {
            aiSuggestion = difyService.analyzeImage(null, firstImageFile);
        }

        // 3. Save DB
        PlantAbnormality record = new PlantAbnormality();
        record.setPlantId(plantId);
        record.setReporterId(reporterId);
        record.setAbnormalityType(type);
        record.setDescription(desc);
        record.setImageUrls(JSON.toJSONString(imageUrls));
        record.setSuggestedSolution(aiSuggestion);
        record.setStatus("PENDING"); // 待分派
        record.setCreatedAt(LocalDateTime.now());
        save(record);

        // 4. Log
        logProcess(record.getId(), reporterId, "USER", "REPORT", "用户上报异常");

        return aiSuggestion;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMaintainer(Long abnormalityId, Long maintainerId, Long operatorId) {
        PlantAbnormality record = getById(abnormalityId);
        if (record == null) throw new RuntimeException("工单不存在");

        record.setMaintainerId(maintainerId);
        record.setStatus("ASSIGNED"); // 已分派
        record.setAssignedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        updateById(record);

        // Log
        logProcess(abnormalityId, operatorId, "ADMIN", "ASSIGN", "分派给养护员ID: " + maintainerId);

        // WebSocket Push to Maintainer
        AbnormalityWebSocket.sendMessage(maintainerId, "您有新的异常工单待处理，ID: " + abnormalityId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveAbnormality(Long abnormalityId, String resolution, String materials, String evaluation, MultipartFile[] images) {
        PlantAbnormality record = getById(abnormalityId);
        if (record == null) throw new RuntimeException("工单不存在");

        // Save Result Images
        List<String> resultUrls = new ArrayList<>();
        if (images != null) {
            try {
                for (MultipartFile img : images) {
                    String fileName = UUID.randomUUID() + "_res_" + img.getOriginalFilename();
                    File dest = new File(uploadPath, fileName);
                    img.transferTo(dest);
                    resultUrls.add("/uploads/" + fileName);
                }
            } catch (IOException e) {
                throw new RuntimeException("图片上传失败", e);
            }
        }

        record.setResolutionDescription(resolution);
        record.setMaterialsUsed(materials);
        record.setEffectEvaluation(evaluation);
        record.setResolutionImageUrls(JSON.toJSONString(resultUrls));
        record.setStatus("RESOLVED"); // 已处理
        record.setResolvedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        updateById(record);

        // Log
        logProcess(abnormalityId, record.getMaintainerId(), "MAINTAINER", "RESOLVE", "养护员完成处理");
        
        // WebSocket Push to Reporter
        AbnormalityWebSocket.sendMessage(record.getReporterId(), "您上报的异常工单已处理，ID: " + abnormalityId);
    }

    @Override
    public int countResolvedByMaintainer(Long maintainerId) {
        return (int) this.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlantAbnormality>()
                .eq(PlantAbnormality::getMaintainerId, maintainerId)
                .eq(PlantAbnormality::getStatus, "RESOLVED"));
    }

    private void logProcess(Long abnormalityId, Long operatorId, String operatorName, String action, String comment) {
        AbnormalityProcessLog log = new AbnormalityProcessLog();
        log.setAbnormalityId(abnormalityId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName); // Should fetch real name
        log.setAction(action);
        log.setComment(comment);
        log.setCreatedAt(LocalDateTime.now());
        processLogMapper.insert(log);
    }
}
