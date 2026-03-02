package com.schoolplant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.schoolplant.dto.AdoptionApplyRequest;
import com.schoolplant.dto.AdoptionQueryRequest;
import com.schoolplant.dto.AdoptionStatusResponse;
import com.schoolplant.dto.AuditApplicationRequest;
import com.schoolplant.entity.AdoptionApplication;
import com.schoolplant.entity.AdoptionAuditLog;
import com.schoolplant.entity.AdoptionRecord;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.vo.AdoptionApplicationVO;
import java.util.List;

public interface AdoptionService extends IService<AdoptionRecord> {
    
    /**
     * 检查用户认养状态
     * @param userId 用户ID
     * @return 状态响应
     */
    AdoptionStatusResponse checkStatus(Long userId);

    /**
     * 提交认养申请
     * @param userId 用户ID
     * @param request 申请信息
     * @return 申请ID
     */
    Long submitApplication(Long userId, AdoptionApplyRequest request);

    /**
     * 获取待审核申请数量
     * @return 数量
     */
    long countPendingAudits();

    /**
     * 查询认养申请列表 VO (Admin/User)
     * @param request 查询条件
     * @return 分页结果
     */
    Page<AdoptionApplicationVO> listApplicationVOs(AdoptionQueryRequest request);

    /**
     * 查询认养申请列表 (Admin) - Deprecated
     * @param request 查询条件
     * @return 分页结果
     */
    Page<AdoptionApplication> listApplications(AdoptionQueryRequest request);

    /**
     * 审核认养申请 (Admin)
     * @param adminId 审核人ID
     * @param request 审核请求
     */
    void auditApplication(Long adminId, AuditApplicationRequest request);

    /**
     * 获取申请的审核记录
     * @param applicationId 申请ID
     * @return 审核日志列表
     */
    List<AdoptionAuditLog> listAuditLogs(Long applicationId);

    /**
     * 申请认养 (Old direct method, maybe deprecate or keep for admin?)
     * Keeping for compatibility if needed, but requirements focus on application flow.
     * @param userId 用户ID
     * @param plantId 植物ID
     * @return 认养记录ID
     */
    Long applyAdoption(Long userId, Long plantId);

    /**
     * 取消认养（包括主动取消和被动惩罚）
     * @param recordId 记录ID
     * @param reason 原因
     */
    void cancelAdoption(Long recordId, String reason);
}
