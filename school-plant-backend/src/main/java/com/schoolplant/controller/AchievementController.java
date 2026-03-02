package com.schoolplant.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.common.R;
import com.schoolplant.entity.Achievement;
import com.schoolplant.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/achievement")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    // 我的成果
    @SaCheckLogin
    @GetMapping("/my")
    public R<Achievement> my(@RequestParam String semester) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(achievementService.getMyAchievement(semester, userId));
    }
    
    // 优秀榜单 (公开)
    @GetMapping("/outstanding")
    public R<Page<Achievement>> outstanding(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam String semester) {
        Page<Achievement> pageParam = new Page<>(page, size);
        return R.ok(achievementService.page(pageParam, 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Achievement>()
                .eq("semester", semester)
                .eq("is_outstanding", 1)
                .orderByDesc("completion_rate")));
    }
    
    // 手动触发生成 (管理员)
    @SaCheckRole("ADMIN")
    @PostMapping("/generate")
    public R<Void> generate(@RequestParam String semester) {
        achievementService.generateSemesterReport(semester);
        return R.ok();
    }
}
