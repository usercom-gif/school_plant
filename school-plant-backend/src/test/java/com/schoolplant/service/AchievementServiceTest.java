package com.schoolplant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.schoolplant.entity.Achievement;
import com.schoolplant.entity.CareTask;
import com.schoolplant.entity.User;
import com.schoolplant.mapper.AchievementMapper;
import com.schoolplant.mapper.CareTaskMapper;
import com.schoolplant.mapper.UserMapper;
import com.schoolplant.service.impl.AchievementServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AchievementServiceTest {

    @InjectMocks
    private AchievementServiceImpl achievementService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CareTaskMapper taskMapper;

    @Mock
    private AchievementMapper achievementMapper;

    @Test
    public void testGenerateSemesterReport() {
        // Mock User
        User user = new User();
        user.setId(100L);
        when(userMapper.selectList(any())).thenReturn(Collections.singletonList(user));

        // Mock Tasks Count (Total 10, Completed 10 -> 100%)
        when(taskMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(10L)  // Total
                .thenReturn(10L); // Completed

        // Mock Existence Check (Not exists)
        when(achievementMapper.selectOne(any())).thenReturn(null);

        // Execute
        achievementService.generateSemesterReport("2024-Spring");

        // Verify Save/Update
        verify(achievementMapper, times(1)).insert(argThat(a -> 
            a.getTaskCompletionRate().compareTo(new BigDecimal("100.00")) == 0 &&
            a.getIsOutstanding() == 1
        ));
    }
}
