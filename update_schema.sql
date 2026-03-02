-- Add missing columns for Plants
ALTER TABLE `plants` ADD COLUMN `care_tips` TEXT COMMENT '养护要点';
ALTER TABLE `plants` ADD COLUMN `growth_cycle` VARCHAR(50) COMMENT '生长周期';

-- Add missing columns for Task Templates
ALTER TABLE `task_templates` ADD COLUMN `operation_requirements` TEXT COMMENT '操作要求';
ALTER TABLE `task_templates` ADD COLUMN `score_standard` TEXT COMMENT '评分标准';
ALTER TABLE `task_templates` ADD COLUMN `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用';

-- Add status column to roles if not exists (already done in previous task but just in case)
-- ALTER TABLE `roles` ADD COLUMN `status` tinyint DEFAULT 1 COMMENT '角色状态：1-启用，0-禁用';
