package com.schoolplant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.schoolplant.dto.*;
import com.schoolplant.entity.TaskTemplate;
import com.schoolplant.vo.TaskTemplateVO;

import java.util.List;

public interface TaskTemplateService extends IService<TaskTemplate> {
    Page<TaskTemplateVO> getTemplateList(TemplateQueryRequest request);
    TaskTemplateVO getTemplateDetail(Long id);
    void addTemplate(TemplateAddRequest request);
    void updateTemplate(TemplateUpdateRequest request);
    void deleteTemplates(List<Long> ids);
    void changeStatus(TemplateStatusRequest request);
}
