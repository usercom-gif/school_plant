package com.schoolplant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.schoolplant.dto.KnowledgePostAddRequest;
import com.schoolplant.dto.KnowledgePostQueryRequest;
import com.schoolplant.dto.KnowledgePostUpdateRequest;
import com.schoolplant.entity.KnowledgePost;
import com.schoolplant.vo.KnowledgePostVO;

import java.util.List;

public interface KnowledgePostService extends IService<KnowledgePost> {
    Page<KnowledgePostVO> getPostList(KnowledgePostQueryRequest request);
    KnowledgePostVO getPostDetail(Long id);
    void addPost(KnowledgePostAddRequest request);
    void updatePost(KnowledgePostUpdateRequest request);
    void deletePost(Long id);
    void auditPost(Long id, boolean pass, String rejectionReason);
    void toggleLike(Long id);
    void reportPost(Long id, String reason);
    void toggleFeature(Long id, boolean isFeatured);
}
