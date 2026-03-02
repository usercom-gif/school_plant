-- Fix table structure for operation_logs
-- Ensure all columns match Entity definition

-- Check and add 'module' column if missing (though DROP/CREATE should handle it, ALTER is safer for existing data)
-- But since user said data exists but schema is wrong, maybe they created it manually or from old script.
-- Let's just alter to add missing columns.

ALTER TABLE `operation_logs` ADD COLUMN `module` varchar(50) DEFAULT NULL COMMENT '所属模块' AFTER `operator_role`;
ALTER TABLE `operation_logs` ADD COLUMN `operation_desc` varchar(255) DEFAULT NULL COMMENT '操作描述' AFTER `operation_type`;

-- Also ensure operator_role exists
ALTER TABLE `operation_logs` ADD COLUMN `operator_role` varchar(50) DEFAULT NULL COMMENT '操作人角色' AFTER `operator_name`;

-- Modify operation_content to be TEXT/JSON compatible if needed (JSON type is fine in MySQL 5.7+)
ALTER TABLE `operation_logs` MODIFY COLUMN `operation_content` JSON DEFAULT NULL COMMENT '操作内容(JSON)';
