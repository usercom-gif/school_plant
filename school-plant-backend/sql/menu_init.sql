-- 1. Update Passwords to Double MD5 (MD5(MD5("123456")))
-- Original: e10adc3949ba59abbe56e057f20f883e
-- New: 14e1b600b1fd579f47433b88e8d85291
UPDATE `users` SET `password` = '14e1b600b1fd579f47433b88e8d85291';

-- 2. Create Menus Table
DROP TABLE IF EXISTS `sys_menus`;
CREATE TABLE `sys_menus` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `name` varchar(50) NOT NULL COMMENT '菜单名称',
  `path` varchar(200) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `type` char(1) DEFAULT 'M' COMMENT '类型（M目录 C菜单 F按钮）',
  `visible` char(1) DEFAULT '0' COMMENT '显示状态（0显示 1隐藏）',
  `status` char(1) DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 3. Create Role-Menu Table
DROP TABLE IF EXISTS `sys_role_menus`;
CREATE TABLE `sys_role_menus` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 4. Insert Menu Data
-- USER Menus (IDs 1-20)
INSERT INTO `sys_menus` VALUES (1, 0, '个人中心', '/profile', 'system/user/profile', 'user:view', 'UserOutlined', 1, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (2, 0, '植物信息查询', '/plant/query', 'plant/index', 'plant:list', 'SearchOutlined', 2, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (3, 0, '认养申请', '/adoption/apply', 'adoption/apply', 'adoption:apply', 'FormOutlined', 3, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (4, 0, '养护任务处理', '/task/handle', 'task/handle', 'task:list', 'CheckSquareOutlined', 4, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (5, 0, '植物异常上报', '/abnormality/report', 'abnormality/report', 'abnormality:report', 'WarningOutlined', 5, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (6, 0, '个人成果查询', '/achievement/query', 'achievement/query', 'achievement:view', 'TrophyOutlined', 6, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (7, 0, '知识共享', '/knowledge', 'knowledge/index', 'knowledge:view', 'ReadOutlined', 7, 'C', '0', '0', NOW(), NOW());

-- ADMIN Menus (IDs 21-40)
INSERT INTO `sys_menus` VALUES (21, 0, '系统管理', '/system', 'Layout', 'system:manage', 'SettingOutlined', 99, 'M', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (22, 21, '用户管理', 'user', 'system/user/index', 'user:list', 'UserOutlined', 1, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (23, 21, '角色管理', 'role', 'system/role/index', 'system:role:manage', 'TeamOutlined', 2, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (24, 21, '菜单管理', 'menu', 'system/menu/index', 'system:menu:manage', 'MenuOutlined', 3, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (25, 21, '系统参数', 'config', 'system/config/index', 'system:param:view', 'ToolOutlined', 4, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (26, 21, '操作日志', 'log', 'system/log/index', 'system:log:view', 'FileTextOutlined', 5, 'C', '0', '0', NOW(), NOW());

INSERT INTO `sys_menus` VALUES (30, 0, '植物管理', '/plant/admin', 'plant/admin', 'plant:edit', 'AppstoreOutlined', 20, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (31, 0, '认养审核', '/adoption/audit', 'adoption/audit', 'adoption:approve', 'AuditOutlined', 21, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (32, 0, '任务管理', '/task/admin', 'task/admin', 'task:generate', 'ScheduleOutlined', 22, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (33, 0, '异常分派', '/abnormality/dispatch', 'abnormality/dispatch', 'abnormality:assign', 'SendOutlined', 23, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (34, 0, '成果评比', '/achievement/review', 'achievement/review', 'achievement:evaluate', 'StarOutlined', 24, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (35, 0, '知识审核', '/knowledge/audit', 'knowledge/audit', 'knowledge:delete', 'SafetyCertificateOutlined', 25, 'C', '0', '0', NOW(), NOW());

-- MAINTAINER Menus (IDs 41-50)
INSERT INTO `sys_menus` VALUES (41, 0, '异常处理', '/abnormality/handle', 'abnormality/handle', 'abnormality:resolve', 'MedicineBoxOutlined', 10, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (42, 0, '任务跟踪', '/task/track', 'task/track', 'task:view', 'EyeOutlined', 11, 'C', '0', '0', NOW(), NOW());
INSERT INTO `sys_menus` VALUES (43, 0, '处理记录', '/personal/record', 'personal/record', 'abnormality:list', 'HistoryOutlined', 12, 'C', '0', '0', NOW(), NOW());

-- 5. Insert Role-Menu Relations
-- Role 1: USER (Menus 1-7)
INSERT INTO `sys_role_menus` VALUES (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7);

-- Role 2: ADMIN (All Menus)
INSERT INTO `sys_role_menus` SELECT 2, id FROM `sys_menus`;

-- Role 3: MAINTAINER (Menus 41-43)
INSERT INTO `sys_role_menus` VALUES (3, 41), (3, 42), (3, 43);
