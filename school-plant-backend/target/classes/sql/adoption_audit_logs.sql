-- ----------------------------
-- Table structure for adoption_audit_logs
-- ----------------------------
DROP TABLE IF EXISTS `adoption_audit_logs`;
CREATE TABLE `adoption_audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审核记录ID',
  `application_id` bigint NOT NULL COMMENT '认养申请ID',
  `auditor_id` bigint NOT NULL COMMENT '审核人ID',
  `auditor_name` varchar(50) DEFAULT NULL COMMENT '审核人姓名',
  `audit_stage` varchar(20) NOT NULL COMMENT '审核阶段: INITIAL-初审, REVIEW-复审, FINAL-终审',
  `audit_action` varchar(20) NOT NULL COMMENT '审核动作: PASS-通过, REJECT-驳回',
  `comment` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_auditor_id` (`auditor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认养申请审核记录表';

-- Add status column comment update (optional, just for documentation)
-- ALTER TABLE `adoption_applications` MODIFY COLUMN `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待审核, INITIAL_PASSED-初审通过, REVIEW_PASSED-复审通过, APPROVED-终审通过(已认养), REJECTED-已驳回, CANCELLED-已取消';
