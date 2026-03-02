package com.schoolplant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.schoolplant.entity.HealthReport;
import com.schoolplant.mapper.HealthReportMapper;
import com.schoolplant.service.HealthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HealthServiceImpl extends ServiceImpl<HealthReportMapper, HealthReport> implements HealthService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long report(Long userId, Long plantId, String desc, String imageUrls) {
        HealthReport report = new HealthReport();
        report.setReporterId(userId);
        report.setPlantId(plantId);
        report.setDescription(desc);
        report.setImageUrls(imageUrls);
        report.setStatus("PENDING");
        report.setIsReminded(0);
        this.save(report);
        
        // TODO: 调用 Mock AI 接口识别病害类型，并更新到 report 中（这里省略 Mock 调用）
        
        // TODO: WebSocket 通知所有在线后勤人员
        
        return report.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolve(Long reportId, Long handlerId, String result) {
        HealthReport report = this.getById(reportId);
        if (report == null) return;
        
        report.setStatus("RESOLVED");
        report.setHandlerId(handlerId);
        report.setHandledAt(LocalDateTime.now());
        // 记录处理结果 result 到某个字段（假设 description 或新字段）
        
        this.updateById(report);
        
        // TODO: 通知上报用户
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkTimeoutReports() {
        // 规则：提交超过48小时且状态仍为 PENDING
        LocalDateTime threshold = LocalDateTime.now().minusHours(48);
        
        List<HealthReport> list = this.list(new LambdaQueryWrapper<HealthReport>()
                .eq(HealthReport::getStatus, "PENDING")
                .eq(HealthReport::getIsReminded, 0)
                .lt(HealthReport::getCreatedAt, threshold));
                
        for (HealthReport report : list) {
            // 触发二次提醒
            report.setIsReminded(1);
            this.updateById(report);
            
            System.out.println("【严重告警】植物异常工单 " + report.getId() + " 已超48小时未处理，请管理员介入！");
            // TODO: 发送邮件给管理员
        }
    }
}
