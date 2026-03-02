package com.schoolplant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.dto.TemplateQueryRequest;
import com.schoolplant.entity.TaskTemplate;
import com.schoolplant.mapper.CareTaskMapper;
import com.schoolplant.mapper.PlantMapper;
import com.schoolplant.mapper.TaskTemplateMapper;
import com.schoolplant.vo.TaskTemplateVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskTemplateServiceImplTest {

    @InjectMocks
    private TaskTemplateServiceImpl templateService;

    @Mock
    private TaskTemplateMapper templateMapper;

    @Mock
    private PlantMapper plantMapper;

    @Mock
    private CareTaskMapper careTaskMapper;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(templateService, "baseMapper", templateMapper);
    }

    @Test
    public void testGetTemplateList() {
        TemplateQueryRequest request = new TemplateQueryRequest();
        request.setPage(1);
        request.setSize(10);

        TaskTemplate template = new TaskTemplate();
        template.setId(1L);
        template.setTaskType("Watering");

        Page<TaskTemplate> page = new Page<>();
        page.setRecords(Collections.singletonList(template));
        page.setTotal(1);

        when(templateMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<TaskTemplateVO> result = templateService.getTemplateList(request);

        assertEquals(1, result.getTotal());
        assertEquals("Watering", result.getRecords().get(0).getTaskType());
    }
}
