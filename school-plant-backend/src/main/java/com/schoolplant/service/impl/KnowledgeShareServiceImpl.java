package com.schoolplant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.schoolplant.entity.KnowledgeLike;
import com.schoolplant.entity.KnowledgeShare;
import com.schoolplant.mapper.KnowledgeLikeMapper;
import com.schoolplant.mapper.KnowledgeShareMapper;
import com.schoolplant.service.KnowledgeShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeShareServiceImpl extends ServiceImpl<KnowledgeShareMapper, KnowledgeShare> implements KnowledgeShareService {

    @Autowired
    private KnowledgeLikeMapper likeMapper;
    
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long shareId, Long userId) {
        // Check if already liked
        Long count = likeMapper.selectCount(new LambdaQueryWrapper<KnowledgeLike>()
                .eq(KnowledgeLike::getShareId, shareId)
                .eq(KnowledgeLike::getUserId, userId));
        if (count > 0) {
            throw new RuntimeException("您已经点过赞了");
        }
        
        // Add like record
        KnowledgeLike like = new KnowledgeLike();
        like.setShareId(shareId);
        like.setUserId(userId);
        likeMapper.insert(like);
        
        // Update share like count
        KnowledgeShare share = this.getById(shareId);
        share.setLikeCount(share.getLikeCount() + 1);
        
        // Check recommend (>= 20 likes)
        if (share.getLikeCount() >= 20) {
            share.setIsRecommend(1);
        }
        
        this.updateById(share);
    }
    
    @Override
    public void view(Long shareId) {
        // Use Redis HyperLogLog for unique view count or simple increment
        // Here simple update for simplicity, in production use Redis
        KnowledgeShare share = this.getById(shareId);
        if (share != null) {
            share.setViewCount(share.getViewCount() + 1);
            this.updateById(share);
        }
    }
}
