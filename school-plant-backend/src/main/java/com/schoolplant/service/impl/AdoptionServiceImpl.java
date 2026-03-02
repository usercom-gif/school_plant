package com.schoolplant.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.schoolplant.dto.AdoptionApplyRequest;
import com.schoolplant.dto.AdoptionQueryRequest;
import com.schoolplant.dto.AdoptionStatusResponse;
import com.schoolplant.dto.AuditApplicationRequest;
import com.schoolplant.entity.AdoptionApplication;
import com.schoolplant.entity.AdoptionAuditLog;
import com.schoolplant.entity.AdoptionRecord;
import com.schoolplant.entity.Plant;
import com.schoolplant.mapper.AdoptionApplicationMapper;
import com.schoolplant.mapper.AdoptionAuditLogMapper;
import com.schoolplant.mapper.AdoptionRecordMapper;
import com.schoolplant.mapper.PlantMapper;
import com.schoolplant.service.AdoptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.schoolplant.vo.AdoptionApplicationVO;
import com.schoolplant.entity.User;
import com.schoolplant.mapper.UserMapper;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;

@Service
public class AdoptionServiceImpl extends ServiceImpl<AdoptionRecordMapper, AdoptionRecord> implements AdoptionService {

    @Autowired
    private PlantMapper plantMapper;

    @Autowired
    private AdoptionRecordMapper adoptionRecordMapper;

    @Autowired
    private AdoptionApplicationMapper adoptionApplicationMapper;

    @Autowired
    private AdoptionAuditLogMapper adoptionAuditLogMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public long countPendingAudits() {
        return adoptionApplicationMapper.selectCount(new LambdaQueryWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getStatus, "PENDING"));
    }

    @Override
    public AdoptionStatusResponse checkStatus(Long userId) {
        AdoptionStatusResponse response = new AdoptionStatusResponse();
        // Check active adoption records
        int activeCount = adoptionRecordMapper.countActiveByUser(userId);
        if (activeCount > 0) {
            response.setCanAdopt(false);
            response.setMessage("您已认养植物，无法再次申请");
            return response;
        }
        
        // Check pending applications (prevent duplicate requests)
        // Check any status that is "in progress" (PENDING, INITIAL_PASSED, REVIEW_PASSED)
        Long pendingCount = adoptionApplicationMapper.selectCount(new LambdaQueryWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getUserId, userId)
                .in(AdoptionApplication::getStatus, "PENDING", "INITIAL_PASSED", "REVIEW_PASSED"));
        if (pendingCount > 0) {
            response.setCanAdopt(false);
            response.setMessage("您有正在审核中的认养申请，请勿重复提交");
            return response;
        }

        response.setCanAdopt(true);
        response.setMessage("可以申请");
        return response;
    }

    @Override
    public Page<AdoptionApplicationVO> listApplicationVOs(AdoptionQueryRequest request) {
        Page<AdoptionApplication> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();
        
        // 权限控制：如果是查询审核列表（即 admin/audit/list 调用的，request.getUserId() 为空通常意味着管理员查询，或者需要区分）
        // 这里需要明确业务场景。
        // 场景1：用户查询自己提交的申请 -> request.getUserId() = currentUserId
        // 场景2：管理员查询所有申请 -> request.getUserId() = null
        // 场景3：普通用户（发布者）查询待审核的申请 -> 只能查自己发布的植物相关的申请
        
        // 我们可以约定，如果 request.getUserId() 不为空，则是查申请人。
        // 如果为空，我们需要判断当前用户是否是管理员。如果不传 userId 且不是管理员，则应当只能查自己发布的植物的申请。
        
        // 但目前 controller 层的逻辑是：
        // /adoption/my-applications -> 传了 userId (当前登录人)
        // /adoption/audit/list -> 没传 userId
        
        // 修改 /adoption/audit/list 的逻辑，使其支持普通发布者查询。
        // 在 Controller 层已经做了 @SaCheckRole("ADMIN") 限制吗？
        // 原来是 @SaCheckRole("ADMIN")，现在需要改为 @SaCheckLogin，并在 Service 层过滤。
        
        Long currentLoginId = null;
        try {
            if (StpUtil.isLogin()) {
                currentLoginId = StpUtil.getLoginIdAsLong();
            }
        } catch (Exception e) {}

        if (request.getUserId() != null) {
            // 查询指定申请人的（我的申请）
            wrapper.eq(AdoptionApplication::getUserId, request.getUserId());
        } else {
            // 查询审核列表
            if (currentLoginId != null && !StpUtil.hasRole("ADMIN")) {
                // 普通用户只能看自己发布植物的申请
                // 子查询：select id from plants where created_by = currentLoginId
                // application.plant_id in (...)
                wrapper.inSql(AdoptionApplication::getPlantId, "SELECT id FROM plants WHERE created_by = " + currentLoginId);
            }
            // 管理员看所有（不做额外限制）
        }

        if (request.getPlantId() != null) {
            wrapper.eq(AdoptionApplication::getPlantId, request.getPlantId());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(AdoptionApplication::getStatus, request.getStatus());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(AdoptionApplication::getCreatedAt, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(AdoptionApplication::getCreatedAt, request.getEndTime());
        }
        
        wrapper.orderByDesc(AdoptionApplication::getCreatedAt);
        
        Page<AdoptionApplication> resultPage = adoptionApplicationMapper.selectPage(page, wrapper);
        
        Page<AdoptionApplicationVO> voPage = new Page<>();
        BeanUtils.copyProperties(resultPage, voPage, "records");
        
        List<AdoptionApplicationVO> voList = resultPage.getRecords().stream().map(app -> {
            AdoptionApplicationVO vo = new AdoptionApplicationVO();
            BeanUtils.copyProperties(app, vo);
            
            // Populate User Name
            User user = userMapper.selectById(app.getUserId());
            if (user != null) {
                vo.setUserName(user.getRealName());
            } else {
                vo.setUserName("未知用户");
            }
            
            // Populate Plant Name
            Plant plant = plantMapper.selectById(app.getPlantId());
            if (plant != null) {
                vo.setPlantName(plant.getName());
            } else {
                vo.setPlantName("未知植物");
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<AdoptionApplication> listApplications(AdoptionQueryRequest request) {
        Page<AdoptionApplication> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();
        if (request.getUserId() != null) wrapper.eq(AdoptionApplication::getUserId, request.getUserId());
        if (StringUtils.hasText(request.getStatus())) wrapper.eq(AdoptionApplication::getStatus, request.getStatus());
        wrapper.orderByDesc(AdoptionApplication::getCreatedAt);
        return adoptionApplicationMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditApplication(Long adminId, AuditApplicationRequest request) {
        AdoptionApplication app = adoptionApplicationMapper.selectById(request.getId());
        if (app == null) {
            throw new RuntimeException("申请不存在");
        }
        
        // 权限校验：植物发布者 或 管理员
        Plant plant = plantMapper.selectById(app.getPlantId());
        if (plant == null) throw new RuntimeException("关联植物不存在");
        
        if (!StpUtil.hasRole("ADMIN")) {
            if (!plant.getCreatedBy().equals(adminId)) {
                throw new RuntimeException("无权审核此申请（非植物发布者）");
            }
        }

        String currentStatus = app.getStatus();
        String action = request.getAction(); // PASS, REJECT
        String nextStatus;
        String stageName;

        // Determine current stage and next status
        if ("PENDING".equals(currentStatus)) {
            stageName = "INITIAL";
            if ("PASS".equals(action)) {
                nextStatus = "INITIAL_PASSED";
            } else {
                nextStatus = "REJECTED";
            }
        } else if ("INITIAL_PASSED".equals(currentStatus)) {
            stageName = "REVIEW";
            if ("PASS".equals(action)) {
                nextStatus = "REVIEW_PASSED";
            } else {
                nextStatus = "REJECTED";
            }
        } else if ("REVIEW_PASSED".equals(currentStatus)) {
            stageName = "FINAL";
            if ("PASS".equals(action)) {
                nextStatus = "APPROVED";
            } else {
                nextStatus = "REJECTED";
            }
        } else {
            throw new RuntimeException("当前状态无法审核: " + currentStatus);
        }

        // Update Application
        app.setStatus(nextStatus);
        app.setUpdatedAt(LocalDateTime.now());
        if ("REJECTED".equals(nextStatus)) {
            app.setRejectionReason(request.getComment());
        }
        if ("APPROVED".equals(nextStatus)) {
            app.setApprovedBy(adminId);
            app.setApprovedAt(LocalDateTime.now());
            
            // Create Adoption Record automatically
            createAdoptionRecord(app);
        }
        adoptionApplicationMapper.updateById(app);

        // Record Audit Log
        AdoptionAuditLog log = new AdoptionAuditLog();
        log.setApplicationId(app.getId());
        log.setAuditorId(adminId);
        // Ideally fetch auditor name, simplified for now
        log.setAuditorName("Admin-" + adminId); 
        log.setAuditStage(stageName);
        log.setAuditAction(action);
        log.setComment(request.getComment());
        log.setCreatedAt(LocalDateTime.now());
        adoptionAuditLogMapper.insert(log);
        
        // TODO: Send Notification (Mock)
        System.out.println("Application " + app.getId() + " status changed to " + nextStatus);
    }

    private void createAdoptionRecord(AdoptionApplication app) {
        // Double check if plant is still available
        Plant plant = plantMapper.selectById(app.getPlantId());
        if ("ADOPTED".equals(plant.getStatus())) {
             // Edge case: plant taken during audit
             // This is tricky. Should we fail the audit? Or just not create record?
             // For simplicity, fail.
             throw new RuntimeException("审核通过，但植物已被认养");
        }
        
        // Update Plant
        plant.setStatus("ADOPTED");
        plantMapper.updateById(plant);

        // Create Record
        AdoptionRecord record = new AdoptionRecord();
        record.setUserId(app.getUserId());
        record.setPlantId(app.getPlantId());
        record.setStartDate(LocalDate.now());
        // Default period from application or 6 months
        int months = app.getAdoptionPeriodMonths() != null ? app.getAdoptionPeriodMonths() : 6;
        record.setEndDate(LocalDate.now().plusMonths(months));
        record.setStatus("ACTIVE");
        adoptionRecordMapper.insert(record);
    }

    @Override
    public List<AdoptionAuditLog> listAuditLogs(Long applicationId) {
        return adoptionAuditLogMapper.selectList(new LambdaQueryWrapper<AdoptionAuditLog>()
                .eq(AdoptionAuditLog::getApplicationId, applicationId)
                .orderByAsc(AdoptionAuditLog::getCreatedAt));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitApplication(Long userId, AdoptionApplyRequest request) {
        // Double check status
        AdoptionStatusResponse status = checkStatus(userId);
        if (!status.isCanAdopt()) {
            throw new RuntimeException(status.getMessage());
        }

        Plant plant = plantMapper.selectById(request.getPlantId());
        if (plant == null) {
            throw new RuntimeException("植物不存在");
        }
        if ("ADOPTED".equals(plant.getStatus())) {
            throw new RuntimeException("该植物已被认养");
        }

        AdoptionApplication app = new AdoptionApplication();
        app.setUserId(userId);
        app.setPlantId(request.getPlantId());
        app.setAdoptionPeriodMonths(request.getAdoptionPeriodMonths());
        app.setCareExperience(request.getCareExperience());
        app.setStatus("PENDING");
        
        adoptionApplicationMapper.insert(app);
        return app.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyAdoption(Long userId, Long plantId) {
        // 1. 检查植物状态
        Plant plant = plantMapper.selectById(plantId);
        if (plant == null) {
            throw new RuntimeException("植物不存在");
        }
        if ("ADOPTED".equals(plant.getStatus())) {
            throw new RuntimeException("该植物已被认养");
        }

        // 2. 检查用户是否已认养（每人限一株）
        // 使用数据库虚拟列 uk_user_active 约束兜底，但这里先做逻辑校验
        int activeCount = adoptionRecordMapper.countActiveByUser(userId);
        if (activeCount > 0) {
            throw new RuntimeException("每人限认养一株植物，您已有生效中的认养记录");
        }

        // 3. 更新植物状态
        plant.setStatus("ADOPTED");
        plantMapper.updateById(plant);

        // 4. 创建认养记录
        AdoptionRecord record = new AdoptionRecord();
        record.setUserId(userId);
        record.setPlantId(plantId);
        record.setStartDate(LocalDate.now());
        record.setEndDate(LocalDate.now().plusMonths(6)); // 默认一学期
        record.setStatus("ACTIVE");
        this.save(record);

        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAdoption(Long recordId, String reason) {
        AdoptionRecord record = this.getById(recordId);
        if (record == null) return;
        if (!"ACTIVE".equals(record.getStatus())) return;

        // 更新记录状态
        record.setStatus("CANCELLED");
        this.updateById(record);

        // 释放植物
        Plant plant = plantMapper.selectById(record.getPlantId());
        if (plant != null) {
            plant.setStatus("HEALTHY");
            plantMapper.updateById(plant);
        }
        
        // TODO: 发送通知给用户
        System.out.println("用户 " + record.getUserId() + " 的认养已取消，原因：" + reason);
    }
}
