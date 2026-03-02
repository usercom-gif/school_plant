-- 1. Add new columns to the plants table
ALTER TABLE plants
ADD COLUMN user_real_name VARCHAR(255) COMMENT '发布者姓名',
ADD COLUMN user_phone VARCHAR(50) COMMENT '发布者电话';

-- 2. Update existing records by joining with the users table based on created_by (user ID)
UPDATE plants p
JOIN users u ON p.created_by = u.id
SET p.user_real_name = u.real_name,
    p.user_phone = u.phone
WHERE p.user_real_name IS NULL OR p.user_phone IS NULL;
