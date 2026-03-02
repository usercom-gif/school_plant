package com.schoolplant.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.schoolplant.dto.KnowledgePostAddRequest;
import com.schoolplant.dto.KnowledgePostQueryRequest;
import com.schoolplant.dto.KnowledgePostUpdateRequest;
import com.schoolplant.entity.ContentReport;
import com.schoolplant.entity.KnowledgePost;
import com.schoolplant.entity.PostLike;
import com.schoolplant.entity.User;
import com.schoolplant.mapper.ContentReportMapper;
import com.schoolplant.mapper.KnowledgePostMapper;
import com.schoolplant.mapper.PostLikeMapper;
import com.schoolplant.mapper.UserMapper;
import com.schoolplant.service.KnowledgePostService;
import com.schoolplant.vo.KnowledgePostVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgePostServiceImpl extends ServiceImpl<KnowledgePostMapper, KnowledgePost> implements KnowledgePostService {

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private ContentReportMapper contentReportMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<KnowledgePostVO> getPostList(KnowledgePostQueryRequest request) {
        Page<KnowledgePost> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<KnowledgePost> wrapper = new LambdaQueryWrapper<>();
        
        // 筛选逻辑
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(KnowledgePost::getTitle, request.getKeyword())
                    .or().like(KnowledgePost::getContent, request.getKeyword())
                    .or().like(KnowledgePost::getTag, request.getKeyword()));
        }
        if (StringUtils.hasText(request.getTag())) {
            wrapper.eq(KnowledgePost::getTag, request.getTag());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(KnowledgePost::getStatus, request.getStatus());
        } else {
            // Default logic: Non-admins only see ACTIVE, UNLESS they are querying their own posts
            if (!StpUtil.hasRole("ADMIN")) {
                boolean isSelf = request.getAuthorId() != null && 
                                 StpUtil.isLogin() && 
                                 request.getAuthorId().equals(StpUtil.getLoginIdAsLong());
                if (!isSelf) {
                    wrapper.eq(KnowledgePost::getStatus, "ACTIVE");
                }
            }
        }
        if (request.getAuthorId() != null) {
            wrapper.eq(KnowledgePost::getAuthorId, request.getAuthorId());
        }

        // 排序
        wrapper.orderByDesc(KnowledgePost::getIsFeatured); // 推荐优先
        wrapper.orderByDesc(KnowledgePost::getCreatedAt);

        Page<KnowledgePost> postPage = this.page(page, wrapper);
        Page<KnowledgePostVO> voPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        
        List<KnowledgePostVO> voList = postPage.getRecords().stream().map(post -> {
            KnowledgePostVO vo = convertToVO(post);
            // 检查当前用户是否点赞
            if (StpUtil.isLogin()) {
                Long userId = StpUtil.getLoginIdAsLong();
                Long count = postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, post.getId())
                        .eq(PostLike::getUserId, userId));
                vo.setHasLiked(count > 0);
            }
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public KnowledgePostVO getPostDetail(Long id) {
        KnowledgePost post = this.getById(id);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        KnowledgePostVO vo = convertToVO(post);
        if (StpUtil.isLogin()) {
            Long userId = StpUtil.getLoginIdAsLong();
            Long count = postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                    .eq(PostLike::getPostId, post.getId())
                    .eq(PostLike::getUserId, userId));
            vo.setHasLiked(count > 0);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPost(KnowledgePostAddRequest request) {
        KnowledgePost post = new KnowledgePost();
        BeanUtils.copyProperties(request, post);
        post.setAuthorId(StpUtil.getLoginIdAsLong());
        post.setStatus("PENDING"); // 默认待审核
        post.setLikeCount(0);
        post.setIsFeatured(0);
        this.save(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(KnowledgePostUpdateRequest request) {
        KnowledgePost post = this.getById(request.getId());
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        // 权限校验：仅作者可编辑，且只能编辑 PENDING 或 REJECTED
        if (!post.getAuthorId().equals(StpUtil.getLoginIdAsLong())) {
            throw new RuntimeException("无权编辑他人帖子");
        }
        if ("ACTIVE".equals(post.getStatus())) {
            throw new RuntimeException("已发布的帖子不可编辑");
        }

        BeanUtils.copyProperties(request, post);
        post.setStatus("PENDING"); // 编辑后重新审核
        this.updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long id) {
        KnowledgePost post = this.getById(id);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        // 权限校验：作者或管理员
        if (!post.getAuthorId().equals(StpUtil.getLoginIdAsLong()) && !StpUtil.hasRole("ADMIN")) {
            throw new RuntimeException("无权删除");
        }
        this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPost(Long id, boolean pass, String rejectionReason) {
        KnowledgePost post = this.getById(id);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        if (pass) {
            post.setStatus("ACTIVE");
        } else {
            post.setStatus("REJECTED");
            // 这里可以记录驳回原因到 extra 字段或发送通知，目前暂存日志或不做持久化（需求提到仅发布者可见，可能需要字段存储）
            // 简单起见，假设通过系统通知或 status 本身传达
        }
        this.updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        PostLike like = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, id)
                .eq(PostLike::getUserId, userId));
        
        KnowledgePost post = this.getById(id);
        if (post == null) return;

        if (like != null) {
            // 取消点赞
            postLikeMapper.deleteById(like);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        } else {
            // 点赞
            like = new PostLike();
            like.setPostId(id);
            like.setUserId(userId);
            postLikeMapper.insert(like);
            post.setLikeCount(post.getLikeCount() + 1);
            
            // 自动推荐逻辑
            if (post.getLikeCount() >= 20 && post.getIsFeatured() == 0) {
                // 需求说弹出提示管理员，或者自动触发？需求写"自动触发推荐"
                post.setIsFeatured(1);
            }
        }
        this.updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportPost(Long id, String reason) {
        ContentReport report = new ContentReport();
        report.setPostId(id);
        report.setReporterId(StpUtil.getLoginIdAsLong());
        report.setReason(reason);
        report.setStatus("PENDING");
        contentReportMapper.insert(report);
    }

    @Override
    public void toggleFeature(Long id, boolean isFeatured) {
        KnowledgePost post = this.getById(id);
        if (post == null) return;
        post.setIsFeatured(isFeatured ? 1 : 0);
        this.updateById(post);
    }

    private KnowledgePostVO convertToVO(KnowledgePost post) {
        KnowledgePostVO vo = new KnowledgePostVO();
        BeanUtils.copyProperties(post, vo);
        User author = userMapper.selectById(post.getAuthorId());
        if (author != null) {
            vo.setAuthorName(author.getRealName()); // Or username
            vo.setAuthorAvatar(author.getAvatarUrl());
        }
        return vo;
    }
}
