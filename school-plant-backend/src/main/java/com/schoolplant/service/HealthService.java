package com.schoolplant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.schoolplant.entity.HealthReport;

public interface HealthService extends IService<HealthReport> {
    
    /**
     * 提交异常上报
     */
    Long report(Long userId, Long plantId, String desc, String imageUrls);

    /**
     * 后勤处理异常
     */
    void resolve(Long reportId, Long handlerId, String result);

    /**
     * 检查48小时未处理的工单，触发二次提醒
     */
    void checkTimeoutReports();
}
