package com.schoolplant.config;

import com.schoolplant.job.AchievementJob;
import com.schoolplant.job.TaskGenerationJob;
import com.schoolplant.job.TaskTimeoutJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    // 1. 每日任务生成：每天凌晨 4:00
    @Bean
    public JobDetail taskGenerationJobDetail() {
        return JobBuilder.newJob(TaskGenerationJob.class)
                .withIdentity("taskGenerationJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger taskGenerationJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(taskGenerationJobDetail())
                .withIdentity("taskGenerationJobTrigger")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(4, 0))
                .build();
    }

    // 2. 逾期任务检查：每天凌晨 2:00
    @Bean
    public JobDetail taskTimeoutJobDetail() {
        return JobBuilder.newJob(TaskTimeoutJob.class)
                .withIdentity("taskTimeoutJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger taskTimeoutJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(taskTimeoutJobDetail())
                .withIdentity("taskTimeoutJobTrigger")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(2, 0))
                .build();
    }

    // 3. 学期末成果评比：每学期末（1月15日/7月15日）
    @Bean
    public JobDetail achievementJobDetail() {
        return JobBuilder.newJob(AchievementJob.class)
                .withIdentity("achievementJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger achievementJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(achievementJobDetail())
                .withIdentity("achievementJobTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 15 1,7 ?"))
                .build();
    }
}
