package com.schoolplant.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.common.R;
import com.schoolplant.entity.KnowledgeShare;
import com.schoolplant.service.KnowledgeShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeShareService knowledgeService;

    // 列表 (分页 + 搜索 + 是否推荐)
    @GetMapping("/list")
    public R<Page<KnowledgeShare>> list(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) Integer isRecommend) {
        Page<KnowledgeShare> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<KnowledgeShare> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(KnowledgeShare::getTitle, keyword).or().like(KnowledgeShare::getContent, keyword);
        }
        if (isRecommend != null) {
            wrapper.eq(KnowledgeShare::getIsRecommend, isRecommend);
        }
        wrapper.orderByDesc(KnowledgeShare::getLikeCount); // 按点赞排序
        
        return R.ok(knowledgeService.page(pageParam, wrapper));
    }
    
    // 发布
    @SaCheckLogin
    @PostMapping("/add")
    public R<Boolean> add(@RequestBody KnowledgeShare share) {
        share.setUserId(StpUtil.getLoginIdAsLong());
        share.setLikeCount(0);
        share.setViewCount(0);
        share.setIsRecommend(0);
        return R.ok(knowledgeService.save(share));
    }
    
    // 点赞
    @SaCheckLogin
    @PostMapping("/like")
    public R<Void> like(@RequestParam Long shareId) {
        Long userId = StpUtil.getLoginIdAsLong();
        knowledgeService.like(shareId, userId);
        return R.ok();
    }
}
