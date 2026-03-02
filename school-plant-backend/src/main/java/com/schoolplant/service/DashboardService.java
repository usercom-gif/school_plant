package com.schoolplant.service;

import com.schoolplant.dto.DashboardStatItem;
import java.util.List;

public interface DashboardService {
    /**
     * 获取仪表盘统计数据
     * @param userId 当前用户ID
     * @param roleKey 当前用户角色Key
     * @return 统计项列表
     */
    List<DashboardStatItem> getStats(Long userId, String roleKey);
}
