-- ----------------------------
-- Table structure for operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '操作日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `operator_role` varchar(50) DEFAULT NULL COMMENT '操作人角色',
  `module` varchar(50) DEFAULT NULL COMMENT '所属模块',
  `operation_type` varchar(50) DEFAULT NULL COMMENT '操作类型',
  `operation_content` json DEFAULT NULL COMMENT '操作内容(JSON)',
  `operation_result` varchar(20) DEFAULT NULL COMMENT '操作结果(SUCCESS/FAILURE)',
  `error_msg` varchar(2000) DEFAULT NULL COMMENT '错误信息',
  `ip_address` varchar(128) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `execution_time` bigint DEFAULT NULL COMMENT '执行时长(ms)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';
