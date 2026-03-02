package com.schoolplant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.schoolplant.dto.OperationLogQueryRequest;
import com.schoolplant.entity.OperationLog;
import com.schoolplant.mapper.OperationLogMapper;
import com.schoolplant.service.OperationLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Async
    @Override
    public void saveLog(OperationLog log) {
        // Here we could add Kafka production logic if configured.
        // For now, save directly to DB asynchronously.
        this.save(log);
    }

    @Override
    public Page<OperationLog> listLogs(OperationLogQueryRequest request) {
        Page<OperationLog> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (request.getUserId() != null) {
            wrapper.eq(OperationLog::getUserId, request.getUserId());
        }
        if (request.getModules() != null && !request.getModules().isEmpty()) {
            wrapper.in(OperationLog::getModule, request.getModules());
        }
        if (request.getOperationType() != null) {
            wrapper.eq(OperationLog::getOperationType, request.getOperationType());
        }
        if (request.getOperationResult() != null) {
            wrapper.eq(OperationLog::getOperationResult, request.getOperationResult());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(OperationLog::getCreatedAt, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(OperationLog::getCreatedAt, request.getEndTime());
        }

        wrapper.orderByDesc(OperationLog::getCreatedAt);
        return this.page(page, wrapper);
    }
}
