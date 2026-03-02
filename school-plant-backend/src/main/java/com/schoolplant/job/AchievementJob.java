package com.schoolplant.job;

import com.schoolplant.service.AchievementService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AchievementJob implements Job {

    @Autowired
    private AchievementService achievementService;

    @Override
    public void execute(JobExecutionContext context) {
        // Calculate current semester
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        String semester = (now.getMonthValue() <= 6) ? year + "-Spring" : year + "-Autumn";
        
        System.out.println("【定时任务】开始生成学期成果报告: " + semester);
        achievementService.generateSemesterReport(semester);
        System.out.println("【定时任务】成果报告生成完成");
    }
}
