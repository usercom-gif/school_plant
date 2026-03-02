package com.schoolplant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.schoolplant.dto.*;
import com.schoolplant.entity.CareTask;
import com.schoolplant.entity.Plant;
import com.schoolplant.entity.TaskTemplate;
import com.schoolplant.mapper.CareTaskMapper;
import com.schoolplant.mapper.PlantMapper;
import com.schoolplant.mapper.TaskTemplateMapper;
import com.schoolplant.service.TaskTemplateService;
import com.schoolplant.vo.TaskTemplateVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskTemplateServiceImpl extends ServiceImpl<TaskTemplateMapper, TaskTemplate> implements TaskTemplateService {

    @Autowired
    private PlantMapper plantMapper;

    @Autowired
    private CareTaskMapper careTaskMapper;

    @Override
    public Page<TaskTemplateVO> getTemplateList(TemplateQueryRequest request) {
        Page<TaskTemplate> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<TaskTemplate> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getPlantSpecies())) {
            wrapper.like(TaskTemplate::getPlantSpecies, request.getPlantSpecies());
        }
        if (StringUtils.hasText(request.getTaskType())) {
            wrapper.eq(TaskTemplate::getTaskType, request.getTaskType());
        }
        if (request.getStatus() != null) {
            wrapper.eq(TaskTemplate::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(TaskTemplate::getCreatedAt);

        Page<TaskTemplate> templatePage = this.page(page, wrapper);
        Page<TaskTemplateVO> voPage = new Page<>(templatePage.getCurrent(), templatePage.getSize(), templatePage.getTotal());
        List<TaskTemplateVO> voList = templatePage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public TaskTemplateVO getTemplateDetail(Long id) {
        TaskTemplate template = this.getById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        return convertToVO(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTemplate(TemplateAddRequest request) {
        // Validate Species
        if (plantMapper.selectCount(new LambdaQueryWrapper<Plant>().eq(Plant::getSpecies, request.getPlantSpecies())) == 0) {
            throw new RuntimeException("植物品种不存在: " + request.getPlantSpecies());
        }

        // Validate Unique (Species + Type)
        if (this.count(new LambdaQueryWrapper<TaskTemplate>()
                .eq(TaskTemplate::getPlantSpecies, request.getPlantSpecies())
                .eq(TaskTemplate::getTaskType, request.getTaskType())) > 0) {
            throw new RuntimeException("该品种已存在同类型任务模板");
        }

        TaskTemplate template = new TaskTemplate();
        BeanUtils.copyProperties(request, template);
        if (template.getStatus() == null) template.setStatus(1);
        this.save(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(TemplateUpdateRequest request) {
        TaskTemplate template = this.getById(request.getId());
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        // Validate Species
        if (plantMapper.selectCount(new LambdaQueryWrapper<Plant>().eq(Plant::getSpecies, request.getPlantSpecies())) == 0) {
            throw new RuntimeException("植物品种不存在: " + request.getPlantSpecies());
        }

        // Validate Unique
        if (this.count(new LambdaQueryWrapper<TaskTemplate>()
                .eq(TaskTemplate::getPlantSpecies, request.getPlantSpecies())
                .eq(TaskTemplate::getTaskType, request.getTaskType())
                .ne(TaskTemplate::getId, request.getId())) > 0) {
            throw new RuntimeException("该品种已存在同类型任务模板");
        }

        BeanUtils.copyProperties(request, template);
        this.updateById(template);
        
        // Requirement: "Update template -> Auto sync to unexecuted tasks"
        // TODO: Sync logic. Find PENDING tasks with this templateId and update description?
        // Usually tasks copy data from template. If we want to sync, we update PENDING tasks.
        CareTask updateTask = new CareTask();
        updateTask.setTaskDescription(template.getTaskDescription());
        // Maybe update due date? No, due date is calculated.
        careTaskMapper.update(updateTask, new LambdaQueryWrapper<CareTask>()
                .eq(CareTask::getTaskTemplateId, template.getId())
                .eq(CareTask::getStatus, "PENDING"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplates(List<Long> ids) {
        for (Long id : ids) {
            // Check association with unfinished tasks
            Long count = careTaskMapper.selectCount(new LambdaQueryWrapper<CareTask>()
                    .eq(CareTask::getTaskTemplateId, id)
                    .eq(CareTask::getStatus, "PENDING"));
            if (count > 0) {
                throw new RuntimeException("模板ID " + id + " 存在未完成的养护任务，无法删除");
            }
        }
        this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(TemplateStatusRequest request) {
        TaskTemplate template = this.getById(request.getId());
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        template.setStatus(request.getStatus());
        this.updateById(template);
    }

    private TaskTemplateVO convertToVO(TaskTemplate template) {
        TaskTemplateVO vo = new TaskTemplateVO();
        BeanUtils.copyProperties(template, vo);
        return vo;
    }
}
