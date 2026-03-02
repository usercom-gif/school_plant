package com.schoolplant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.common.R;
import com.schoolplant.entity.CareTask;
import com.schoolplant.entity.Plant;
import com.schoolplant.entity.User;
import com.schoolplant.service.CareTaskService;
import com.schoolplant.service.PlantService;
import com.schoolplant.service.UserService;
import com.schoolplant.vo.CareTaskVO;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/task")
public class CareTaskController {

    @Autowired
    private CareTaskService careTaskService;
    
    @Autowired
    private UserService userService; // Or UserMapper if Service not available
    
    @Autowired
    private PlantService plantService; // Or PlantMapper

    // 我的今日任务 (User)
    @SaCheckLogin
    @GetMapping("/my-tasks")
    public R<Page<CareTaskVO>> myTasks(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) String status) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<CareTask> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CareTask> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("adopter_id", userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("due_date");
        
        Page<CareTask> result = careTaskService.page(pageParam, wrapper);
                
        return R.ok(convertToVO(result));
    }

    // 任务列表 (Admin)
    @SaCheckRole("ADMIN")
    @GetMapping("/list")
    public R<Page<CareTaskVO>> list(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) Long userId,
                                  @RequestParam(required = false) String status) {
        Page<CareTask> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CareTask> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (userId != null) wrapper.eq("adopter_id", userId);
        if (status != null && !status.isEmpty()) wrapper.eq("status", status);
        wrapper.orderByDesc("created_at");
        
        return R.ok(convertToVO(careTaskService.page(pageParam, wrapper)));
    }

    // 创建任务 (Admin)
    @SaCheckRole("ADMIN")
    @PostMapping("/create")
    public R<Boolean> create(@RequestBody CareTask task) {
        task.setStatus("PENDING");
        return R.ok(careTaskService.save(task));
    }

    // 更新任务 (Admin)
    @SaCheckRole("ADMIN")
    @PutMapping("/update")
    public R<Boolean> update(@RequestBody CareTask task) {
        return R.ok(careTaskService.updateById(task));
    }

    // 删除任务 (Admin)
    @SaCheckRole("ADMIN")
    @DeleteMapping("/{ids}")
    public R<Boolean> delete(@PathVariable List<Long> ids) {
        return R.ok(careTaskService.removeByIds(ids));
    }

    // 完成任务打卡 (User)
    @SaCheckLogin
    @PostMapping("/complete")
    public R<Boolean> complete(@RequestBody CareTask task) {
        if (task.getId() == null) return R.fail("任务ID不能为空");
        CareTask existing = careTaskService.getById(task.getId());
        if (existing == null) return R.fail("任务不存在");
        
        // 校验权限
        if (!existing.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            return R.fail("无权操作他人任务");
        }

        existing.setStatus("COMPLETED");
        existing.setImageUrl(task.getImageUrl());
        existing.setCompletedAt(java.time.LocalDateTime.now());
        
        return R.ok(careTaskService.updateById(existing));
    }
    
    private Page<CareTaskVO> convertToVO(Page<CareTask> page) {
        Page<CareTaskVO> voPage = new Page<>();
        BeanUtils.copyProperties(page, voPage, "records");
        
        List<CareTaskVO> voList = page.getRecords().stream().map(task -> {
            CareTaskVO vo = new CareTaskVO();
            BeanUtils.copyProperties(task, vo);
            
            User user = userService.getById(task.getUserId());
            if (user != null) vo.setUserName(user.getRealName());
            
            Plant plant = plantService.getById(task.getPlantId());
            if (plant != null) vo.setPlantName(plant.getName());
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
}
