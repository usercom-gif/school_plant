CREATE TABLE IF NOT EXISTS `sys_approval` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '申请人ID',
  `type` varchar(50) NOT NULL COMMENT '审批类型: REAL_NAME_CHANGE',
  `original_value` varchar(255) DEFAULT NULL COMMENT '原值',
  `new_value` varchar(255) DEFAULT NULL COMMENT '新值',
  `reason` varchar(500) DEFAULT NULL COMMENT '申请理由',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING, APPROVED, REJECTED',
  `approver_id` bigint DEFAULT NULL COMMENT '审批人ID',
  `comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';
