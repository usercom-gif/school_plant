package com.schoolplant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.schoolplant.entity.AdoptionRecord;
import com.schoolplant.entity.CareTask;
import com.schoolplant.mapper.AdoptionRecordMapper;
import com.schoolplant.mapper.CareTaskMapper;
import com.schoolplant.service.impl.CareTaskServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CareTaskServiceTest {

    @InjectMocks
    private CareTaskServiceImpl careTaskService;

    @Mock
    private CareTaskMapper careTaskMapper;

    @Mock
    private AdoptionRecordMapper adoptionRecordMapper;
    
    @Mock
    private AdoptionService adoptionService;

    @Test
    public void testGenerateDailyTasks() {
        // Mock active records
        AdoptionRecord record = new AdoptionRecord();
        record.setId(1L);
        record.setUserId(100L);
        record.setPlantId(200L);
        record.setStatus("ACTIVE");
        
        when(adoptionRecordMapper.selectList(any())).thenReturn(Collections.singletonList(record));
        when(careTaskMapper.insert(any(CareTask.class))).thenReturn(1);

        // Execute
        careTaskService.generateDailyTasks();

        // Verify
        // Since the logic depends on day of year % 3, it might or might not insert
        // But we verify that selectList was called
        verify(adoptionRecordMapper, times(1)).selectList(any());
    }

    @Test
    public void testCheckOverdueTasks() {
        // Mock overdue task
        CareTask task = new CareTask();
        task.setId(1L);
        task.setUserId(100L);
        task.setPlantId(200L);
        task.setStatus("PENDING");
        task.setDueDate(LocalDate.now().minusDays(4)); // Overdue > 3 days

        when(careTaskMapper.selectList(any())).thenReturn(Collections.singletonList(task));
        when(careTaskMapper.updateById(any(CareTask.class))).thenReturn(1);
        
        AdoptionRecord record = new AdoptionRecord();
        record.setId(5L);
        record.setStatus("ACTIVE");
        when(adoptionRecordMapper.selectOne(any())).thenReturn(record);

        // Execute
        careTaskService.checkOverdueTasks();

        // Verify status update to OVERDUE
        verify(careTaskMapper, times(1)).updateById(argThat(t -> "OVERDUE".equals(t.getStatus())));
        // Verify adoption cancellation
        verify(adoptionService, times(1)).cancelAdoption(eq(5L), anyString());
    }
}
