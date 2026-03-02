package com.schoolplant.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.annotation.Log;
import com.schoolplant.common.R;
import com.schoolplant.dto.KnowledgePostAddRequest;
import com.schoolplant.dto.KnowledgePostQueryRequest;
import com.schoolplant.dto.KnowledgePostUpdateRequest;
import com.schoolplant.service.KnowledgePostService;
import com.schoolplant.vo.KnowledgePostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Knowledge Post Management", description = "知识帖子管理")
@RestController
@RequestMapping("/knowledge/post")
public class KnowledgePostController {

    @Autowired
    private KnowledgePostService postService;

    @Operation(summary = "获取帖子列表")
    @GetMapping("/list")
    public R<Page<KnowledgePostVO>> list(KnowledgePostQueryRequest request) {
        return R.ok(postService.getPostList(request));
    }

    @Operation(summary = "获取帖子详情")
    @GetMapping("/{id}")
    public R<KnowledgePostVO> getInfo(@PathVariable Long id) {
        return R.ok(postService.getPostDetail(id));
    }

    @Operation(summary = "发布帖子")
    @SaCheckLogin
    @Log(module = "KNOWLEDGE", title = "发布知识帖子", operationType = "INSERT")
    @PostMapping
    public R<Void> add(@Validated @RequestBody KnowledgePostAddRequest request) {
        postService.addPost(request);
        return R.ok();
    }

    @Operation(summary = "修改帖子")
    @SaCheckLogin
    @Log(module = "KNOWLEDGE", title = "修改知识帖子", operationType = "UPDATE")
    @PutMapping
    public R<Void> update(@Validated @RequestBody KnowledgePostUpdateRequest request) {
        postService.updatePost(request);
        return R.ok();
    }

    @Operation(summary = "删除帖子")
    @SaCheckLogin
    @Log(module = "KNOWLEDGE", title = "删除知识帖子", operationType = "DELETE")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        postService.deletePost(id);
        return R.ok();
    }

    @Operation(summary = "审核帖子")
    @SaCheckRole("ADMIN")
    @Log(module = "KNOWLEDGE", title = "审核知识帖子", operationType = "AUDIT")
    @PutMapping("/audit")
    public R<Void> audit(@RequestParam Long id, @RequestParam boolean pass, @RequestParam(required = false) String reason) {
        postService.auditPost(id, pass, reason);
        return R.ok();
    }

    @Operation(summary = "点赞/取消点赞")
    @SaCheckLogin
    @PostMapping("/{id}/like")
    public R<Void> toggleLike(@PathVariable Long id) {
        postService.toggleLike(id);
        return R.ok();
    }

    @Operation(summary = "推荐/取消推荐")
    @SaCheckRole("ADMIN")
    @Log(module = "KNOWLEDGE", title = "推荐知识帖子", operationType = "UPDATE")
    @PutMapping("/{id}/feature")
    public R<Void> toggleFeature(@PathVariable Long id, @RequestParam boolean isFeatured) {
        postService.toggleFeature(id, isFeatured);
        return R.ok();
    }

    @Operation(summary = "举报帖子")
    @SaCheckLogin
    @Log(module = "KNOWLEDGE", title = "举报知识帖子", operationType = "INSERT")
    @PostMapping("/{id}/report")
    public R<Void> report(@PathVariable Long id, @RequestParam String reason) {
        postService.reportPost(id, reason);
        return R.ok();
    }
}
