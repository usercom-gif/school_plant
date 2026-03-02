package com.schoolplant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.schoolplant.entity.CareTask;

public interface CareTaskService extends IService<CareTask> {
    void generateDailyTasks();
    void checkOverdueTasks();
}
