package com.schoolplant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.schoolplant.entity.AdoptionApplication;
import com.schoolplant.entity.CareTask;
import com.schoolplant.entity.TaskTemplate;
import com.schoolplant.mapper.AdoptionApplicationMapper;
import com.schoolplant.mapper.CareTaskMapper;
import com.schoolplant.mapper.TaskTemplateMapper;
import com.schoolplant.service.CareTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CareTaskServiceImpl extends ServiceImpl<CareTaskMapper, CareTask> implements CareTaskService {

    @Autowired
    private CareTaskMapper careTaskMapper;

    @Autowired
    private AdoptionApplicationMapper adoptionMapper;

    @Autowired
    private TaskTemplateMapper templateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateDailyTasks() {
        // 1. Get all active adoptions (APPROVED)
        List<AdoptionApplication> activeAdoptions = adoptionMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getStatus, "APPROVED")
        );

        // 2. For each adoption, check templates and generate tasks
        for (AdoptionApplication adoption : activeAdoptions) {
            // Find plant species (Need join or separate query, here simplified)
            // Assuming we fetch plant species
            String species = "Succulent"; // Placeholder
            
            List<TaskTemplate> templates = templateMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskTemplate>()
                    .eq(TaskTemplate::getPlantSpecies, species)
            );
            
            for (TaskTemplate template : templates) {
                // Check if task should be generated today based on frequency
                // Simple logic: if (dayOfYear % frequency == 0)
                if (LocalDate.now().getDayOfYear() % template.getFrequencyDays() == 0) {
                    CareTask task = new CareTask();
                    task.setPlantId(adoption.getPlantId());
                    task.setUserId(adoption.getUserId());
                    task.setTaskTemplateId(template.getId());
                    task.setTaskType(template.getTaskType());
                    task.setTaskDescription(template.getTaskDescription());
                    task.setDueDate(LocalDate.now().plusDays(1));
                    task.setStatus("PENDING");
                    this.save(task);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOverdueTasks() {
        LocalDate threshold = LocalDate.now().minusDays(3);
        List<CareTask> overdueTasks = careTaskMapper.findOverdueTasks(threshold);
        
        for (CareTask task : overdueTasks) {
            task.setStatus("OVERDUE");
            this.updateById(task);
            
            // Trigger penalty: cancel adoption
            adoptionMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getUserId, task.getUserId())
                .eq(AdoptionApplication::getPlantId, task.getPlantId())
                .set(AdoptionApplication::getStatus, "CANCELLED"));
        }
    }
}
