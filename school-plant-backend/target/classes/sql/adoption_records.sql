-- ----------------------------
-- Table structure for adoption_records
-- ----------------------------
DROP TABLE IF EXISTS `adoption_records`;
CREATE TABLE `adoption_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '认养记录ID，主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `plant_id` bigint NOT NULL COMMENT '植物ID',
  `start_date` date NOT NULL COMMENT '认养开始日期',
  `end_date` date NOT NULL COMMENT '认养结束日期',
  `status` enum('ACTIVE','FINISHED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-进行中，FINISHED-已结束，CANCELLED-已取消',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删，1-已删',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_plant_id` (`plant_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `adoption_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `adoption_records_ibfk_2` FOREIGN KEY (`plant_id`) REFERENCES `plants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认养记录表';

-- ----------------------------
-- Records of adoption_records
-- (Migrating data from existing adoption_applications if approved, or sample data)
-- ----------------------------
BEGIN;
-- Sample active record based on adoption_applications id=1 (Approved)
INSERT INTO `adoption_records` (`user_id`, `plant_id`, `start_date`, `end_date`, `status`, `created_at`, `updated_at`) 
VALUES (2, 1, '2026-01-01', '2026-06-30', 'ACTIVE', '2026-01-01 10:30:00', '2026-01-01 10:30:00');
COMMIT;
