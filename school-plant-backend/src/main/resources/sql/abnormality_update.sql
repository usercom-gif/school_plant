-- Update PlantAbnormality table
ALTER TABLE `plant_abnormalities` ADD COLUMN `resolution_image_urls` TEXT COMMENT '处理后照片JSON' AFTER `resolution_description`;
ALTER TABLE `plant_abnormalities` ADD COLUMN `assigned_at` DATETIME COMMENT '分派时间' AFTER `status`;
ALTER TABLE `plant_abnormalities` ADD COLUMN `materials_used` VARCHAR(255) COMMENT '使用材料' AFTER `resolution_description`;
ALTER TABLE `plant_abnormalities` ADD COLUMN `effect_evaluation` TEXT COMMENT '效果评估' AFTER `materials_used`;
ALTER TABLE `plant_abnormalities` ADD COLUMN `overtime_alert_sent` TINYINT(1) DEFAULT 0 COMMENT '是否已发送超时提醒' AFTER `updated_at`;

-- Create AbnormalityProcessLog table
DROP TABLE IF EXISTS `abnormality_process_logs`;
CREATE TABLE `abnormality_process_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `abnormality_id` BIGINT NOT NULL COMMENT '异常ID',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) COMMENT '操作人姓名',
  `action` VARCHAR(50) NOT NULL COMMENT '动作: REPORT, ASSIGN, HANDLE, RESOLVE, CLOSE',
  `comment` TEXT COMMENT '备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_abnormality_id` (`abnormality_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异常处理流程日志';
