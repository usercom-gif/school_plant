package com.schoolplant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.schoolplant.entity.Achievement;

public interface AchievementService extends IService<Achievement> {
    void generateSemesterReport(String semester);
    Achievement getMyAchievement(String semester, Long userId);
}
