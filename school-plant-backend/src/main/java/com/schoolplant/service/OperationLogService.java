package com.schoolplant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.schoolplant.dto.OperationLogQueryRequest;
import com.schoolplant.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {
    /**
     * 保存日志 (异步)
     */
    void saveLog(OperationLog log);

    /**
     * 查询日志列表
     */
    Page<OperationLog> listLogs(OperationLogQueryRequest request);
}
