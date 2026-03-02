package com.schoolplant.job;

import com.schoolplant.service.CareTaskService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskTimeoutJob implements Job {

    @Autowired
    private CareTaskService careTaskService;

    @Override
    public void execute(JobExecutionContext context) {
        System.out.println("【定时任务】检查逾期养护任务...");
        careTaskService.checkOverdueTasks();
        System.out.println("【定时任务】逾期检查完成");
    }
}
