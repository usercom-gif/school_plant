package com.schoolplant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.schoolplant.entity.KnowledgeShare;

public interface KnowledgeShareService extends IService<KnowledgeShare> {
    void like(Long shareId, Long userId);
    void view(Long shareId);
}
