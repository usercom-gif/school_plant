package com.schoolplant.job;

import com.schoolplant.service.CareTaskService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskGenerationJob implements Job {

    @Autowired
    private CareTaskService careTaskService;

    @Override
    public void execute(JobExecutionContext context) {
        System.out.println("【定时任务】开始生成每日养护任务...");
        careTaskService.generateDailyTasks();
        System.out.println("【定时任务】生成完成");
    }
}
