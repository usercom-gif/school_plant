package com.schoolplant.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.schoolplant.entity.PlantAbnormality;
import com.schoolplant.service.PlantAbnormalityService;
import com.schoolplant.websocket.AbnormalityWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class AbnormalityScheduler {

    @Autowired
    private PlantAbnormalityService abnormalityService;

    // Check every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void checkOvertime() {
        log.info("Checking for overtime abnormalities...");
        LocalDateTime deadline = LocalDateTime.now().minusHours(48);

        List<PlantAbnormality> overdueList = abnormalityService.list(new LambdaQueryWrapper<PlantAbnormality>()
                .eq(PlantAbnormality::getStatus, "ASSIGNED")
                .eq(PlantAbnormality::getOvertimeAlertSent, 0)
                .lt(PlantAbnormality::getAssignedAt, deadline));

        for (PlantAbnormality record : overdueList) {
            log.warn("Abnormality {} is overtime!", record.getId());
            
            // 1. Update flag
            record.setOvertimeAlertSent(1);
            abnormalityService.updateById(record);

            // 2. Notify Admin (Assuming Admin ID is 1 or broadcast to all admins)
            // For MVP, just log or send to specific ID if known.
            // AbnormalityWebSocket.sendMessage(1L, "工单超时报警: ID " + record.getId());

            // 3. Notify Maintainer
            AbnormalityWebSocket.sendMessage(record.getMaintainerId(), "【超时提醒】您的工单 ID " + record.getId() + " 已超过48小时未处理！");
        }
    }
}
