package com.schoolplant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.schoolplant.dto.DashboardStatItem;
import com.schoolplant.entity.AdoptionApplication;
import com.schoolplant.entity.CareTask;
import com.schoolplant.entity.KnowledgePost;
import com.schoolplant.entity.PlantAbnormality;
import com.schoolplant.mapper.*;
import com.schoolplant.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PlantMapper plantMapper;
    @Autowired
    private AdoptionApplicationMapper adoptionApplicationMapper;
    @Autowired
    private OperationLogMapper operationLogMapper;
    @Autowired
    private PlantAbnormalityMapper plantAbnormalityMapper;
    @Autowired
    private CareTaskMapper careTaskMapper;
    @Autowired
    private AdoptionRecordMapper adoptionRecordMapper;
    @Autowired
    private KnowledgePostMapper knowledgePostMapper;

    @Override
    public List<DashboardStatItem> getStats(Long userId, String roleKey) {
        List<DashboardStatItem> stats = new ArrayList<>();

        if ("ADMIN".equals(roleKey)) {
            // 用户总数
            Long userCount = userMapper.selectCount(null);
            stats.add(new DashboardStatItem("用户总数", userCount, "UserOutlined", "#3f8600"));

            // 植物总数
            Long plantCount = plantMapper.selectCount(null);
            stats.add(new DashboardStatItem("植物总数", plantCount, "AppstoreOutlined", "#cf1322"));

            // 待办审核 (PENDING, INITIAL_PASSED, REVIEW_PASSED)
            Long auditCount = adoptionApplicationMapper.selectCount(new LambdaQueryWrapper<AdoptionApplication>()
                    .in(AdoptionApplication::getStatus, "PENDING", "INITIAL_PASSED", "REVIEW_PASSED"));
            stats.add(new DashboardStatItem("待办审核", auditCount, "AuditOutlined", "#1890ff"));

            // 系统日志
            Long logCount = operationLogMapper.selectCount(null);
            stats.add(new DashboardStatItem("系统日志", logCount, "FileTextOutlined", "#722ed1"));

        } else if ("MAINTAINER".equals(roleKey)) {
            // 待处理异常 (Assigned to maintainer)
            Long abnormalityCount = plantAbnormalityMapper.selectCount(new LambdaQueryWrapper<PlantAbnormality>()
                    .eq(PlantAbnormality::getMaintainerId, userId)
                    .in(PlantAbnormality::getStatus, "ASSIGNED", "REPORTED"));
            stats.add(new DashboardStatItem("待处理异常", abnormalityCount, "WarningOutlined", "#cf1322"));

            // 今日任务
            Long todayTaskCount = careTaskMapper.selectCount(new LambdaQueryWrapper<CareTask>()
                    .eq(CareTask::getUserId, userId)
                    .eq(CareTask::getDueDate, LocalDate.now())
                    .eq(CareTask::getStatus, "PENDING"));
            stats.add(new DashboardStatItem("今日任务", todayTaskCount, "ScheduleOutlined", "#1890ff"));

            // 本月完成
            LocalDateTime startOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
            Long monthCompleted = careTaskMapper.selectCount(new LambdaQueryWrapper<CareTask>()
                    .eq(CareTask::getUserId, userId)
                    .eq(CareTask::getStatus, "COMPLETED")
                    .between(CareTask::getUpdatedAt, startOfMonth, endOfMonth));
            stats.add(new DashboardStatItem("本月完成", monthCompleted, "CheckCircleOutlined", "#3f8600"));

            // 负责区域 (暂无数据，返回0)
            stats.add(new DashboardStatItem("负责区域", 0, "EnvironmentOutlined", "#faad14"));

        } else {
            // USER (Student/Teacher)
            // 已认养
            int adoptedCount = adoptionRecordMapper.countActiveByUser(userId);
            stats.add(new DashboardStatItem("已认养", adoptedCount, "HeartOutlined", "#cf1322"));

            // 我的积分 (暂无，0)
            stats.add(new DashboardStatItem("我的积分", 0, "TrophyOutlined", "#faad14"));

            // 养护任务 (Pending)
            Long pendingTasks = careTaskMapper.selectCount(new LambdaQueryWrapper<CareTask>()
                    .eq(CareTask::getUserId, userId)
                    .eq(CareTask::getStatus, "PENDING"));
            stats.add(new DashboardStatItem("养护任务", pendingTasks, "ToolOutlined", "#1890ff"));

            // 分享文章
            Long postCount = knowledgePostMapper.selectCount(new LambdaQueryWrapper<KnowledgePost>()
                    .eq(KnowledgePost::getAuthorId, userId));
            stats.add(new DashboardStatItem("分享文章", postCount, "ReadOutlined", "#3f8600"));
        }

        return stats;
    }
}
