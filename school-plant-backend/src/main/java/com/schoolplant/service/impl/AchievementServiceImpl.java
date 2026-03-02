package com.schoolplant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.schoolplant.entity.Achievement;
import com.schoolplant.entity.CareTask;
import com.schoolplant.entity.User;
import com.schoolplant.mapper.AchievementMapper;
import com.schoolplant.mapper.CareTaskMapper;
import com.schoolplant.mapper.UserMapper;
import com.schoolplant.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class AchievementServiceImpl extends ServiceImpl<AchievementMapper, Achievement> implements AchievementService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CareTaskMapper taskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateSemesterReport(String semester) {
        // 1. Get all users
        List<User> users = userMapper.selectList(null);
        
        for (User user : users) {
            // 2. Count total tasks (COMPLETED + OVERDUE)
            Long total = taskMapper.selectCount(new LambdaQueryWrapper<CareTask>()
                    .eq(CareTask::getUserId, user.getId())
                    .in(CareTask::getStatus, "COMPLETED", "OVERDUE")
            );
            
            if (total == 0) continue;
            
            // 3. Count completed tasks
            Long completed = taskMapper.selectCount(new LambdaQueryWrapper<CareTask>()
                    .eq(CareTask::getUserId, user.getId())
                    .eq(CareTask::getStatus, "COMPLETED")
            );
            
            // 4. Calculate rate
            BigDecimal rate = BigDecimal.ZERO;
            if (total > 0) {
                rate = new BigDecimal(completed)
                        .divide(new BigDecimal(total), 2, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
            }
            
            // 5. Check existence
            Achievement achievement = this.getOne(new LambdaQueryWrapper<Achievement>()
                    .eq(Achievement::getUserId, user.getId())
                    .eq(Achievement::getSemester, semester));
            
            if (achievement == null) {
                achievement = new Achievement();
                achievement.setUserId(user.getId());
                achievement.setSemester(semester);
            }
            
            achievement.setTotalTasks(total.intValue());
            achievement.setCompletedTasks(completed.intValue());
            achievement.setCompletionRate(rate);
            
            // 6. Evaluate
            if (rate.compareTo(new BigDecimal("100.00")) == 0) {
                achievement.setIsOutstanding(1);
                achievement.setCertificateUrl("http://oss.example.com/cert/" + user.getId() + "_" + semester + ".pdf");
            } else {
                achievement.setIsOutstanding(0);
                achievement.setCertificateUrl(null);
            }
            
            this.saveOrUpdate(achievement);
        }
    }

    @Override
    public Achievement getMyAchievement(String semester, Long userId) {
        return this.getOne(new LambdaQueryWrapper<Achievement>()
                .eq(Achievement::getUserId, userId)
                .eq(Achievement::getSemester, semester));
    }
}
