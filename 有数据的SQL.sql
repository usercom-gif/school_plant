/*
 Navicat Premium Dump SQL

 Source Server         : Thesis
 Source Server Type    : MySQL
 Source Server Version : 90300 (9.3.0)
 Source Host           : localhost:3306
 Source Schema         : campus_plant_system

 Target Server Type    : MySQL
 Target Server Version : 90300 (9.3.0)
 File Encoding         : 65001

 Date: 01/03/2026 23:10:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for abnormality_process_logs
-- ----------------------------
DROP TABLE IF EXISTS `abnormality_process_logs`;
CREATE TABLE `abnormality_process_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `abnormality_id` bigint NOT NULL COMMENT '异常ID',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人姓名',
  `action` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动作: REPORT, ASSIGN, HANDLE, RESOLVE, CLOSE',
  `comment` text COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_abnormality_id` (`abnormality_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异常处理流程日志';

-- ----------------------------
-- Records of abnormality_process_logs
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for achievements
-- ----------------------------
DROP TABLE IF EXISTS `achievements`;
CREATE TABLE `achievements` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成就记录ID，主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `plant_id` bigint NOT NULL COMMENT '认养植物ID',
  `adoption_start_date` date NOT NULL COMMENT '认养开始日期',
  `adoption_end_date` date DEFAULT NULL COMMENT '认养结束日期',
  `tasks_completed` int DEFAULT '0' COMMENT '已完成任务数',
  `total_tasks` int DEFAULT '0' COMMENT '总任务数',
  `task_completion_rate` decimal(5,2) DEFAULT '0.00' COMMENT '任务完成率（百分比）',
  `plant_health_score` int DEFAULT '0' COMMENT '植物健康度评分（1-100）',
  `is_outstanding` tinyint DEFAULT '0' COMMENT '是否优秀养护人：0-否，1-是',
  `certificate_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电子荣誉证书URL',
  `semester` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学期（如2025-Spring）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `plant_id` (`plant_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_semester` (`semester`),
  KEY `idx_is_outstanding` (`is_outstanding`),
  CONSTRAINT `achievements_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `achievements_ibfk_2` FOREIGN KEY (`plant_id`) REFERENCES `plants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认养成果与评比表';

-- ----------------------------
-- Records of achievements
-- ----------------------------
BEGIN;
INSERT INTO `achievements` (`id`, `user_id`, `plant_id`, `adoption_start_date`, `adoption_end_date`, `tasks_completed`, `total_tasks`, `task_completion_rate`, `plant_health_score`, `is_outstanding`, `certificate_url`, `semester`, `created_at`, `updated_at`) VALUES (1, 2, 1, '2026-01-01', '2026-01-26', 0, 2, 0.00, 85, 0, NULL, '2026-Spring', '2026-01-03 16:01:24', '2026-01-26 18:40:47');
INSERT INTO `achievements` (`id`, `user_id`, `plant_id`, `adoption_start_date`, `adoption_end_date`, `tasks_completed`, `total_tasks`, `task_completion_rate`, `plant_health_score`, `is_outstanding`, `certificate_url`, `semester`, `created_at`, `updated_at`) VALUES (2, 2, 4, '2025-09-01', '2025-12-31', 24, 25, 96.00, 92, 1, 'https://example.com/certificate/wang_xiaoming_fall2025.pdf', '2025-Fall', '2026-01-03 16:01:24', '2026-01-03 16:01:24');
COMMIT;

-- ----------------------------
-- Table structure for adoption_applications
-- ----------------------------
DROP TABLE IF EXISTS `adoption_applications`;
CREATE TABLE `adoption_applications` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '认养申请ID，主键',
  `user_id` bigint NOT NULL COMMENT '申请人用户ID',
  `plant_id` bigint NOT NULL COMMENT '申请认养的植物ID',
  `adoption_period_months` int NOT NULL DEFAULT '6' COMMENT '认养周期（月数，默认6个月即1学期）',
  `care_experience` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '养护经验说明',
  `status` enum('PENDING','INITIAL_PASSED','REVIEW_PASSED','APPROVED','REJECTED','CANCELLED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `rejection_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '驳回原因',
  `approved_by` bigint DEFAULT NULL COMMENT '审核人用户ID',
  `approved_at` timestamp NULL DEFAULT NULL COMMENT '审核时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_active_adoption` (`user_id`,`status`),
  KEY `approved_by` (`approved_by`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_plant_id` (`plant_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `adoption_applications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `adoption_applications_ibfk_2` FOREIGN KEY (`plant_id`) REFERENCES `plants` (`id`) ON DELETE CASCADE,
  CONSTRAINT `adoption_applications_ibfk_3` FOREIGN KEY (`approved_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认养申请表';

-- ----------------------------
-- Records of adoption_applications
-- ----------------------------
BEGIN;
INSERT INTO `adoption_applications` (`id`, `user_id`, `plant_id`, `adoption_period_months`, `care_experience`, `status`, `rejection_reason`, `approved_by`, `approved_at`, `created_at`, `updated_at`) VALUES (1, 2, 1, 6, '我在家中养过樱花盆栽，了解其生长习性和养护要点。可以保证每周定期浇水和观察生长状况。', 'APPROVED', NULL, 1, '2026-01-01 10:30:00', '2026-01-03 16:00:01', '2026-01-03 16:00:01');
INSERT INTO `adoption_applications` (`id`, `user_id`, `plant_id`, `adoption_period_months`, `care_experience`, `status`, `rejection_reason`, `approved_by`, `approved_at`, `created_at`, `updated_at`) VALUES (2, 3, 2, 6, '我对银杏树很感兴趣，虽然没有直接养护经验，但我学习能力强，会认真按照指导进行养护。', 'REJECTED', '学习知识了再来', NULL, NULL, '2026-01-03 16:00:01', '2026-02-28 18:25:13');
INSERT INTO `adoption_applications` (`id`, `user_id`, `plant_id`, `adoption_period_months`, `care_experience`, `status`, `rejection_reason`, `approved_by`, `approved_at`, `created_at`, `updated_at`) VALUES (3, 4, 3, 6, '我家附近就有桂花树，每年都能闻到香味。我了解桂花的基本养护需求，愿意承担认养责任。', 'REJECTED', '下一次', 1, '2026-01-02 14:20:00', '2026-01-03 16:00:01', '2026-02-28 18:23:00');
INSERT INTO `adoption_applications` (`id`, `user_id`, `plant_id`, `adoption_period_months`, `care_experience`, `status`, `rejection_reason`, `approved_by`, `approved_at`, `created_at`, `updated_at`) VALUES (4, 2, 4, 6, '紫薇树已经有人认养了，但我还是想申请试试。', 'REJECTED', '下一次', 1, '2026-01-01 11:00:00', '2026-01-03 16:00:01', '2026-02-28 18:23:05');
INSERT INTO `adoption_applications` (`id`, `user_id`, `plant_id`, `adoption_period_months`, `care_experience`, `status`, `rejection_reason`, `approved_by`, `approved_at`, `created_at`, `updated_at`) VALUES (5, 4, 2, 6, '我在家养过银杏树，我承诺我一定养的好，请放心我', 'INITIAL_PASSED', NULL, NULL, NULL, '2026-02-28 18:55:59', '2026-02-28 18:56:44');
COMMIT;

-- ----------------------------
-- Table structure for adoption_audit_logs
-- ----------------------------
DROP TABLE IF EXISTS `adoption_audit_logs`;
CREATE TABLE `adoption_audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审核记录ID',
  `application_id` bigint NOT NULL COMMENT '认养申请ID',
  `auditor_id` bigint NOT NULL COMMENT '审核人ID',
  `auditor_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核人姓名',
  `audit_stage` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '审核阶段: INITIAL-初审, REVIEW-复审, FINAL-终审',
  `audit_action` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '审核动作: PASS-通过, REJECT-驳回',
  `comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核意见',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_auditor_id` (`auditor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认养申请审核记录表';

-- ----------------------------
-- Records of adoption_audit_logs
-- ----------------------------
BEGIN;
INSERT INTO `adoption_audit_logs` (`id`, `application_id`, `auditor_id`, `auditor_name`, `audit_stage`, `audit_action`, `comment`, `created_at`) VALUES (1, 2, 1, 'Admin-1', 'INITIAL', 'REJECT', '学习知识了再来', '2026-02-28 18:25:13');
INSERT INTO `adoption_audit_logs` (`id`, `application_id`, `auditor_id`, `auditor_name`, `audit_stage`, `audit_action`, `comment`, `created_at`) VALUES (2, 5, 1, 'Admin-1', 'INITIAL', 'PASS', '很好加油', '2026-02-28 18:56:44');
COMMIT;

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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认养记录表';

-- ----------------------------
-- Records of adoption_records
-- ----------------------------
BEGIN;
INSERT INTO `adoption_records` (`id`, `user_id`, `plant_id`, `start_date`, `end_date`, `status`, `created_at`, `updated_at`, `deleted`) VALUES (1, 2, 1, '2026-01-01', '2026-06-30', 'ACTIVE', '2026-01-01 10:30:00', '2026-01-01 10:30:00', 0);
COMMIT;

-- ----------------------------
-- Table structure for care_tasks
-- ----------------------------
DROP TABLE IF EXISTS `care_tasks`;
CREATE TABLE `care_tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '养护任务ID，主键',
  `plant_id` bigint NOT NULL COMMENT '关联植物ID',
  `adopter_id` bigint NOT NULL COMMENT '认养人用户ID',
  `task_template_id` bigint DEFAULT NULL COMMENT '关联任务模板ID',
  `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务类型',
  `task_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务描述',
  `due_date` date NOT NULL COMMENT '截止日期',
  `completed_date` date DEFAULT NULL COMMENT '完成日期',
  `status` enum('PENDING','COMPLETED','OVERDUE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING-待完成，COMPLETED-已完成，OVERDUE-已超时',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '完成任务凭证图片URL',
  PRIMARY KEY (`id`),
  KEY `task_template_id` (`task_template_id`),
  KEY `idx_adopter_id` (`adopter_id`),
  KEY `idx_plant_id` (`plant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_due_date` (`due_date`),
  CONSTRAINT `care_tasks_ibfk_1` FOREIGN KEY (`plant_id`) REFERENCES `plants` (`id`) ON DELETE CASCADE,
  CONSTRAINT `care_tasks_ibfk_2` FOREIGN KEY (`adopter_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `care_tasks_ibfk_3` FOREIGN KEY (`task_template_id`) REFERENCES `task_templates` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='养护任务表';

-- ----------------------------
-- Records of care_tasks
-- ----------------------------
BEGIN;
INSERT INTO `care_tasks` (`id`, `plant_id`, `adopter_id`, `task_template_id`, `task_type`, `task_description`, `due_date`, `completed_date`, `status`, `created_at`, `updated_at`, `deleted`, `image_url`) VALUES (1, 1, 2, 1, '浇水', '彻底浇灌樱花树根部，确保土壤湿润但不积水，每次约20升水。', '2026-01-10', NULL, 'PENDING', '2026-01-03 16:00:01', '2026-01-03 16:00:01', 0, NULL);
INSERT INTO `care_tasks` (`id`, `plant_id`, `adopter_id`, `task_template_id`, `task_type`, `task_description`, `due_date`, `completed_date`, `status`, `created_at`, `updated_at`, `deleted`, `image_url`) VALUES (2, 1, 2, 3, '施肥', '施用复合肥，每株约100克，均匀撒在树冠投影范围内。', '2026-02-15', NULL, 'PENDING', '2026-01-03 16:00:01', '2026-01-03 16:00:01', 0, NULL);
INSERT INTO `care_tasks` (`id`, `plant_id`, `adopter_id`, `task_template_id`, `task_type`, `task_description`, `due_date`, `completed_date`, `status`, `created_at`, `updated_at`, `deleted`, `image_url`) VALUES (3, 2, 3, 4, '浇水', '银杏树耐旱，仅在连续干旱时浇水，每次约15升。', '2026-01-15', NULL, 'PENDING', '2026-01-03 16:00:01', '2026-01-03 16:00:01', 0, NULL);
INSERT INTO `care_tasks` (`id`, `plant_id`, `adopter_id`, `task_template_id`, `task_type`, `task_description`, `due_date`, `completed_date`, `status`, `created_at`, `updated_at`, `deleted`, `image_url`) VALUES (4, 3, 4, 6, '浇水', '桂花喜湿润，保持土壤微湿，每次浇水约15升。', '2026-01-08', NULL, 'PENDING', '2026-01-03 16:00:01', '2026-01-03 16:00:01', 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for classes
-- ----------------------------
DROP TABLE IF EXISTS `classes`;
CREATE TABLE `classes` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '班级ID，主键',
  `grade_id` bigint NOT NULL COMMENT '所属年级ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '班级名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '班级描述',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_grade_id` (`grade_id`),
  CONSTRAINT `classes_ibfk_1` FOREIGN KEY (`grade_id`) REFERENCES `grades` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- ----------------------------
-- Records of classes
-- ----------------------------
BEGIN;
INSERT INTO `classes` (`id`, `grade_id`, `name`, `description`, `created_at`, `updated_at`) VALUES (1, 1, '计算机科学与技术A班', '计算机专业2025级A班', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `classes` (`id`, `grade_id`, `name`, `description`, `created_at`, `updated_at`) VALUES (2, 1, '计算机科学与技术B班', '计算机专业2025级B班', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `classes` (`id`, `grade_id`, `name`, `description`, `created_at`, `updated_at`) VALUES (3, 2, '软件工程A班', '软件工程专业2024级A班', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `classes` (`id`, `grade_id`, `name`, `description`, `created_at`, `updated_at`) VALUES (4, 3, '生物技术A班', '生物技术专业2023级A班', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
COMMIT;

-- ----------------------------
-- Table structure for content_reports
-- ----------------------------
DROP TABLE IF EXISTS `content_reports`;
CREATE TABLE `content_reports` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报记录ID，主键',
  `post_id` bigint NOT NULL COMMENT '被举报帖子ID',
  `reporter_id` bigint NOT NULL COMMENT '举报人用户ID',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报原因',
  `status` enum('PENDING','REVIEWED','RESOLVED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '处理状态：PENDING-待审核，REVIEWED-已审核，RESOLVED-已解决',
  `reviewed_by` bigint DEFAULT NULL COMMENT '审核人用户ID',
  `reviewed_at` timestamp NULL DEFAULT NULL COMMENT '审核时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `reporter_id` (`reporter_id`),
  KEY `reviewed_by` (`reviewed_by`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `content_reports_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `knowledge_posts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `content_reports_ibfk_2` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `content_reports_ibfk_3` FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容举报表';

-- ----------------------------
-- Records of content_reports
-- ----------------------------
BEGIN;
INSERT INTO `content_reports` (`id`, `post_id`, `reporter_id`, `reason`, `status`, `reviewed_by`, `reviewed_at`, `created_at`) VALUES (1, 2, 4, '内容不够详细，缺乏实用性', 'REVIEWED', 1, '2026-01-02 16:00:00', '2026-01-03 16:01:24');
COMMIT;

-- ----------------------------
-- Table structure for grades
-- ----------------------------
DROP TABLE IF EXISTS `grades`;
CREATE TABLE `grades` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '年级ID，主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '年级名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '年级描述',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='年级表';

-- ----------------------------
-- Records of grades
-- ----------------------------
BEGIN;
INSERT INTO `grades` (`id`, `name`, `description`, `created_at`, `updated_at`) VALUES (1, '2025级', '2025级本科生', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `grades` (`id`, `name`, `description`, `created_at`, `updated_at`) VALUES (2, '2024级', '2024级本科生', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `grades` (`id`, `name`, `description`, `created_at`, `updated_at`) VALUES (3, '2023级', '2023级本科生', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
COMMIT;

-- ----------------------------
-- Table structure for knowledge_posts
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_posts`;
CREATE TABLE `knowledge_posts` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识帖子ID，主键',
  `author_id` bigint NOT NULL COMMENT '作者用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子内容',
  `tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签（如#浇水技巧）',
  `is_featured` tinyint DEFAULT '0' COMMENT '是否推荐：0-否，1-是',
  `like_count` int DEFAULT '0' COMMENT '点赞数量',
  `status` enum('ACTIVE','REPORTED','DELETED','PENDING','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_author_id` (`author_id`),
  KEY `idx_tag` (`tag`),
  KEY `idx_is_featured` (`is_featured`),
  KEY `idx_like_count` (`like_count`),
  CONSTRAINT `knowledge_posts_ibfk_1` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='植物知识分享表';

-- ----------------------------
-- Records of knowledge_posts
-- ----------------------------
BEGIN;
INSERT INTO `knowledge_posts` (`id`, `author_id`, `title`, `content`, `tag`, `is_featured`, `like_count`, `status`, `created_at`, `updated_at`) VALUES (1, 2, '樱花树春季养护全攻略', '春季是樱花树生长的关键时期，需要注意以下几点：1. 及时浇水，保持土壤湿润；2. 花后及时修剪，去除残花；3. 施用氮磷钾复合肥；4. 注意蚜虫', '#樱花养护', 1, 4, 'ACTIVE', '2026-01-03 16:01:24', '2026-01-03 16:01:24');
INSERT INTO `knowledge_posts` (`id`, `author_id`, `title`, `content`, `tag`, `is_featured`, `like_count`, `status`, `created_at`, `updated_at`) VALUES (2, 3, '银杏树的四季管理要点', '银杏树是优秀的园林树种，四季管理要点：春季注意防风，夏季适当浇水，秋季及时清理落叶，冬季做好防寒措施。银杏树生长缓慢，需要耐心养护。', '#银杏养护', 0, 4, 'ACTIVE', '2026-01-03 16:01:24', '2026-01-03 16:01:24');
INSERT INTO `knowledge_posts` (`id`, `author_id`, `title`, `content`, `tag`, `is_featured`, `like_count`, `status`, `created_at`, `updated_at`) VALUES (3, 6, '校园植物认养的意义与价值', '参与校园植物认养不仅能美化环境，还能培养我们的责任感和环保意识。通过亲手养护植物，我们能更深入地了解植物的生长规律，体验生命的奇', '#认养意义', 1, 4, 'ACTIVE', '2026-01-03 16:01:24', '2026-01-03 16:01:24');
INSERT INTO `knowledge_posts` (`id`, `author_id`, `title`, `content`, `tag`, `is_featured`, `like_count`, `status`, `created_at`, `updated_at`) VALUES (4, 2, '浇水的正确方法', '浇水不是越多越好！要根据植物种类、季节、天气来调整。一般原则：见干见湿，浇则浇透。避免中午高温时浇水，最好选择早晨或傍晚。', '#浇水技巧', 1, 4, 'ACTIVE', '2026-01-03 16:01:24', '2026-01-03 16:01:24');
INSERT INTO `knowledge_posts` (`id`, `author_id`, `title`, `content`, `tag`, `is_featured`, `like_count`, `status`, `created_at`, `updated_at`) VALUES (5, 4, '多肉植物的日常养护技巧', '多肉植物以其肥厚多汁的叶片和顽强的生命力深受喜爱，日常养护需注意以下几点：\n光照：多肉大多喜光，春秋冬三季可给予充足直射光，夏季需遮阴避免暴晒，防止叶片灼伤。\n浇水：遵循 “干透浇透” 原则，待土壤完全干透后再浇透，避免频繁浇水导致烂根；空气干燥时可向植株周围喷雾增湿，勿直接喷在叶片上。\n土壤：选用疏松透气的颗粒土（如泥炭土 + 珍珠岩 + 火山石按 3:2:1 比例混合），保证排水性，防止积水。\n温度：适宜生长温度为 15-28℃，冬季需保持 5℃以上，避免冻伤；夏季高温时注意通风降温。\n常见问题：若叶片发软发皱，多为缺水；若叶片透明化水，多为浇水过多烂根，需及时脱盆修剪烂根并重新栽种。', '#多肉植物#室内绿植#养护指南', 0, 0, 'ACTIVE', '2026-02-27 01:07:59', '2026-02-27 01:07:59');
COMMIT;

-- ----------------------------
-- Table structure for operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '操作日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人姓名',
  `operator_role` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人角色',
  `module` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属模块',
  `operation_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作类型',
  `operation_content` json DEFAULT NULL COMMENT '操作内容(JSON)',
  `operation_result` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作结果(SUCCESS/FAILURE)',
  `error_msg` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误信息',
  `ip_address` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户代理',
  `execution_time` bigint DEFAULT NULL COMMENT '执行时长(ms)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';

-- ----------------------------
-- Records of operation_logs
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID，主键',
  `permission_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限标识符（如：plant:view, plant:edit, adoption:approve）',
  `permission_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '权限描述',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属模块（user, plant, adoption, task, abnormality, knowledge, achievement, system）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `permission_key` (`permission_key`),
  KEY `idx_module` (`module`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ----------------------------
-- Records of permissions
-- ----------------------------
BEGIN;
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (1, 'user:view', '查看用户信息', '查看用户基本信息', 'user', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (2, 'user:edit', '编辑用户信息', '修改个人信息', 'user', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (3, 'user:delete', '删除用户', '禁用或删除用户账号', 'user', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (4, 'user:list', '查看用户列表', '查看所有用户列表', 'user', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (5, 'plant:view', '查看植物信息', '查看植物详细信息', 'plant', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (6, 'plant:create', '创建植物信息', '添加新植物记录', 'plant', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (7, 'plant:edit', '编辑植物信息', '修改植物信息', 'plant', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (8, 'plant:delete', '删除植物信息', '删除植物记录', 'plant', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (9, 'plant:list', '查看植物列表', '浏览所有植物', 'plant', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (10, 'plant:import', '导入植物数据', '批量导入植物信息', 'plant', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (11, 'plant:export', '导出植物数据', '批量导出植物信息', 'plant', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (12, 'adoption:apply', '提交认养申请', '申请认养植物', 'adoption', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (13, 'adoption:view', '查看认养申请', '查看自己的认养申请', 'adoption', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (14, 'adoption:approve', '审核认养申请', '批准或驳回认养申请', 'adoption', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (15, 'adoption:list', '查看认养列表', '查看所有认养申请', 'adoption', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (16, 'adoption:cancel', '取消认养关系', '主动取消认养', 'adoption', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (17, 'adoption:certificate', '生成认养证书', '生成电子认养证书', 'adoption', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (18, 'task:view', '查看养护任务', '查看分配的任务', 'task', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (19, 'task:complete', '完成养护任务', '标记任务为已完成', 'task', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (20, 'task:list', '查看任务列表', '查看所有相关任务', 'task', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (21, 'task:generate', '生成养护任务', '手动或自动创建任务', 'task', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (22, 'abnormality:report', '上报植物异常', '报告植物健康问题', 'abnormality', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (23, 'abnormality:view', '查看异常报告', '查看自己上报的异常', 'abnormality', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (24, 'abnormality:assign', '分配异常处理', '将异常分配给养护员', 'abnormality', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (25, 'abnormality:resolve', '处理异常问题', '标记异常为已解决', 'abnormality', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (26, 'abnormality:list', '查看异常列表', '查看所有异常报告', 'abnormality', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (27, 'knowledge:create', '发布知识帖子', '分享养护经验', 'knowledge', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (28, 'knowledge:view', '查看知识帖子', '阅读知识分享', 'knowledge', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (29, 'knowledge:edit', '编辑知识帖子', '修改自己的帖子', 'knowledge', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (30, 'knowledge:delete', '删除知识帖子', '删除帖子（自己或管理）', 'knowledge', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (31, 'knowledge:like', '点赞知识帖子', '为帖子点赞', 'knowledge', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (32, 'knowledge:feature', '推荐优质内容', '标记为推荐阅读', 'knowledge', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (33, 'knowledge:report', '举报违规内容', '举报不当内容', 'knowledge', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (34, 'achievement:view', '查看认养成果', '查看个人成果统计', 'achievement', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (35, 'achievement:list', '查看成果排行榜', '查看全校成果排名', 'achievement', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (36, 'achievement:evaluate', '评估认养成果', '进行成果评比', 'achievement', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (37, 'achievement:certificate', '生成荣誉证书', '生成优秀养护人证书', 'achievement', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (38, 'system:param:view', '查看系统参数', '查看系统配置', 'system', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (39, 'system:param:edit', '编辑系统参数', '修改系统配置', 'system', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (40, 'system:log:view', '查看操作日志', '查看系统操作记录', 'system', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `permissions` (`id`, `permission_key`, `permission_name`, `description`, `module`, `created_at`, `updated_at`) VALUES (41, 'system:role:manage', '管理角色权限', '配置角色和权限', 'system', '2026-01-03 15:56:15', '2026-01-03 15:56:15');
COMMIT;

-- ----------------------------
-- Table structure for plant_abnormalities
-- ----------------------------
DROP TABLE IF EXISTS `plant_abnormalities`;
CREATE TABLE `plant_abnormalities` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '异常记录ID，主键',
  `plant_id` bigint NOT NULL COMMENT '关联植物ID',
  `reporter_id` bigint NOT NULL COMMENT '上报人用户ID',
  `maintainer_id` bigint DEFAULT NULL COMMENT '分配的养护员ID',
  `abnormality_type` enum('YELLOW_LEAVES','PESTS','WILTING','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '异常类型：YELLOW_LEAVES-黄叶，PESTS-有虫，WILTR-其他',
  `urgency` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'MEDIUM' COMMENT '紧急程度: HIGH, MEDIUM, LOW',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '异常详细描述',
  `image_urls` json DEFAULT NULL COMMENT '异常图片URL数组（JSON格式）',
  `suggested_solution` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '建议解决方案',
  `status` enum('REPORTED','ASSIGNED','RESOLVED','ESCALATED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'REPORTED' COMMENT '处理状态：REPORTED-已上报，ASSIGNEOLVED-已解决，ESCALATED-已升级',
  `assigned_at` datetime DEFAULT NULL COMMENT '分派时间',
  `resolution_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '处理说明',
  `materials_used` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '使用材料',
  `effect_evaluation` text COLLATE utf8mb4_unicode_ci COMMENT '效果评估',
  `resolution_image_urls` text COLLATE utf8mb4_unicode_ci COMMENT '处理后照片JSON',
  `resolved_at` timestamp NULL DEFAULT NULL COMMENT '解决时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `overtime_alert_sent` tinyint(1) DEFAULT '0' COMMENT '是否已发送超时提醒',
  PRIMARY KEY (`id`),
  KEY `maintainer_id` (`maintainer_id`),
  KEY `idx_plant_id` (`plant_id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `plant_abnormalities_ibfk_1` FOREIGN KEY (`plant_id`) REFERENCES `plants` (`id`) ON DELETE CASCADE,
  CONSTRAINT `plant_abnormalities_ibfk_2` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `plant_abnormalities_ibfk_3` FOREIGN KEY (`maintainer_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='植物健康异常表';

-- ----------------------------
-- Records of plant_abnormalities
-- ----------------------------
BEGIN;
INSERT INTO `plant_abnormalities` (`id`, `plant_id`, `reporter_id`, `maintainer_id`, `abnormality_type`, `urgency`, `description`, `image_urls`, `suggested_solution`, `status`, `assigned_at`, `resolution_description`, `materials_used`, `effect_evaluation`, `resolution_image_urls`, `resolved_at`, `created_at`, `updated_at`, `overtime_alert_sent`) VALUES (1, 1, 2, 5, 'PESTS', 'MEDIUM', '樱花树叶片背面发现大量蚜虫，叶片开始卷曲发黄。', NULL, '建议使用吡虫啉溶液喷洒，浓度1:1000，连续喷洒3天，注意保护益虫。', 'ASSIGNED', NULL, NULL, NULL, NULL, NULL, NULL, '2026-01-03 16:00:01', '2026-01-03 16:00:01', 0);
INSERT INTO `plant_abnormalities` (`id`, `plant_id`, `reporter_id`, `maintainer_id`, `abnormality_type`, `urgency`, `description`, `image_urls`, `suggested_solution`, `status`, `assigned_at`, `resolution_description`, `materials_used`, `effect_evaluation`, `resolution_image_urls`, `resolved_at`, `created_at`, `updated_at`, `overtime_alert_sent`) VALUES (2, 4, 2, 5, 'YELLOW_LEAVES', 'MEDIUM', '紫薇树部分叶片出现黄化现象，可能与土壤pH值有关。', NULL, '建议检测土壤pH值，如偏碱可施用硫酸亚铁调节，同时增加有机肥。', 'REPORTED', NULL, NULL, NULL, NULL, NULL, NULL, '2026-01-03 16:00:01', '2026-01-03 16:00:01', 0);
COMMIT;

-- ----------------------------
-- Table structure for plants
-- ----------------------------
DROP TABLE IF EXISTS `plants`;
CREATE TABLE `plants` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '植物ID，主键',
  `plant_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '植物编号，唯一标识',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '植物名称',
  `species` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '植物品种',
  `number` bigint NOT NULL DEFAULT '0' COMMENT '植物数量',
  `family` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '科属',
  `location_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '位置描述',
  `region` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '校园区域（如中央校区、东校区等）',
  `care_difficulty` tinyint NOT NULL DEFAULT '1' COMMENT '养护难度等级（1-5，1为简单，5为困难）',
  `status` enum('AVAILABLE','ADOPTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AVAILABLE' COMMENT '植物状态：AVAILABLE-待认养，ADOPTED-已认养',
  `planting_year` year DEFAULT NULL COMMENT '种植年份',
  `light_requirement` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '光照需求（全日照、半阴、全阴）',
  `water_requirement` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '水分需求（低、中、高）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '植物详细描述',
  `care_tips` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '养护要点',
  `growth_cycle` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生长周期',
  `image_urls` json DEFAULT NULL COMMENT '植物图片URL数组（JSON格式）',
  `created_by` bigint NOT NULL COMMENT '创建者用户ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间（软删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `plant_code` (`plant_code`),
  KEY `created_by` (`created_by`),
  KEY `idx_status` (`status`),
  KEY `idx_region` (`region`),
  KEY `idx_care_difficulty` (`care_difficulty`),
  KEY `idx_plant_code` (`plant_code`),
  CONSTRAINT `plants_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='植物信息表';

-- ----------------------------
-- Records of plants
-- ----------------------------
BEGIN;
INSERT INTO `plants` (`id`, `plant_code`, `name`, `species`, `number`, `family`, `location_description`, `region`, `care_difficulty`, `status`, `planting_year`, `light_requirement`, `water_requirement`, `description`, `care_tips`, `growth_cycle`, `image_urls`, `created_by`, `created_at`, `updated_at`, `deleted_at`) VALUES (1, 'PLT001', '樱花树', 'Prunus serrulata', 10, '蔷薇科', '主校区图书馆前广场', '中央校区', 3, 'ADOPTED', 2020, '全日照', '中等', '日本晚樱，春季开花，花期3-4月，需要', NULL, NULL, '[\"/profile/d003c59e-619b-49d9-8fdc-01204128a2d3.jpg\"]', 1, '2026-01-03 16:00:01', '2026-01-03 17:01:16', NULL);
INSERT INTO `plants` (`id`, `plant_code`, `name`, `species`, `number`, `family`, `location_description`, `region`, `care_difficulty`, `status`, `planting_year`, `light_requirement`, `water_requirement`, `description`, `care_tips`, `growth_cycle`, `image_urls`, `created_by`, `created_at`, `updated_at`, `deleted_at`) VALUES (2, 'PLT002', '银杏树', 'Ginkgo biloba', 12, '银杏科', '东校区教学楼A栋旁', '东校区', 2, 'AVAILABLE', 2018, '全日照', '低', '古老树种，秋季叶片金黄，抗污染能力强，适合', NULL, NULL, '[\"/profile/bbcc2a92-f917-449e-a2a9-f6deb05881dc.jpg\"]', 1, '2026-01-03 16:00:01', '2026-01-03 17:01:19', NULL);
INSERT INTO `plants` (`id`, `plant_code`, `name`, `species`, `number`, `family`, `location_description`, `region`, `care_difficulty`, `status`, `planting_year`, `light_requirement`, `water_requirement`, `description`, `care_tips`, `growth_cycle`, `image_urls`, `created_by`, `created_at`, `updated_at`, `deleted_at`) VALUES (3, 'PLT003', '桂花树', 'Osmanthus fragrans', 20, '木犀科', '西校区宿舍区花园', '西校区', 2, 'AVAILABLE', 2019, '半阴', '中等', '常绿灌木，秋季开花，香气浓郁，需要定期', NULL, NULL, '[]', 1, '2026-01-03 16:00:01', '2026-01-03 17:01:25', NULL);
INSERT INTO `plants` (`id`, `plant_code`, `name`, `species`, `number`, `family`, `location_description`, `region`, `care_difficulty`, `status`, `planting_year`, `light_requirement`, `water_requirement`, `description`, `care_tips`, `growth_cycle`, `image_urls`, `created_by`, `created_at`, `updated_at`, `deleted_at`) VALUES (4, 'PLT004', '紫薇树', 'Lagerstroemia indica', 23, '千屈菜科', '南校区运动场边', '南校区', 3, 'ADOPTED', 2021, '全日照', '中等', '夏季开花，花期长，耐旱耐热，需要定期', NULL, NULL, '[\"/profile/4a8b4590-9237-4e94-8f58-6fca61e9510e.jpg\"]', 1, '2026-01-03 16:00:01', '2026-01-03 17:01:29', NULL);
INSERT INTO `plants` (`id`, `plant_code`, `name`, `species`, `number`, `family`, `location_description`, `region`, `care_difficulty`, `status`, `planting_year`, `light_requirement`, `water_requirement`, `description`, `care_tips`, `growth_cycle`, `image_urls`, `created_by`, `created_at`, `updated_at`, `deleted_at`) VALUES (5, 'PLT005', '竹子', 'Bambusa multiplex', 28, '禾本科', '北校区湖边', '北校区', 2, 'AVAILABLE', 2017, '半阴', '高', '丛生竹，生长迅速，需要控制蔓延，定期疏笋。', NULL, NULL, '[]', 1, '2026-01-03 16:00:01', '2026-01-03 17:01:34', NULL);
COMMIT;

-- ----------------------------
-- Table structure for post_likes
-- ----------------------------
DROP TABLE IF EXISTS `post_likes`;
CREATE TABLE `post_likes` (
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '点赞用户ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`post_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `post_likes_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `knowledge_posts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `post_likes_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞表';

-- ----------------------------
-- Records of post_likes
-- ----------------------------
BEGIN;
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (1, 2, '2026-01-03 16:01:24');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (1, 3, '2026-01-03 16:01:24');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (1, 4, '2026-01-03 16:01:24');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (1, 6, '2026-01-03 16:01:24');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (2, 2, '2026-01-03 16:01:24');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (2, 3, '2026-01-03 16:01:24');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (2, 4, '2026-02-27 00:01:27');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (2, 6, '2026-01-03 16:01:24');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (3, 2, '2026-01-03 16:01:24');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (3, 3, '2026-01-03 16:01:24');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (3, 4, '2026-01-03 16:01:24');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (3, 6, '2026-01-03 16:01:24');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (4, 2, '2026-01-03 16:01:24');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (4, 3, '2026-01-03 16:01:24');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (4, 4, '2026-01-03 16:01:24');
INSERT INTO `post_likes` (`post_id`, `user_id`, `created_at`) VALUES (4, 6, '2026-01-03 16:01:24');
COMMIT;

-- ----------------------------
-- Table structure for role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `permission_id` (`permission_id`),
  CONSTRAINT `role_permissions_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `role_permissions_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ----------------------------
-- Records of role_permissions
-- ----------------------------
BEGIN;
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 1, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 2, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 5, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 9, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 12, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 13, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 16, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 18, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 19, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 20, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 22, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 23, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 27, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 28, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 29, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 31, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 33, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 34, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (1, 35, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 1, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 2, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 3, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 4, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 5, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 6, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 7, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 8, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 9, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 10, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 11, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 12, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 13, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 14, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 15, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 16, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 17, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 18, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 19, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 20, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 21, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 22, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 23, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 24, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 25, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 26, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 27, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 28, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 29, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 30, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 31, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 32, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 33, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 34, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 35, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 36, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 37, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 38, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 39, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 40, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (2, 41, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (3, 1, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (3, 5, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (3, 9, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (3, 13, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (3, 15, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (3, 18, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (3, 20, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (3, 23, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (3, 24, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (3, 25, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (3, 26, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (3, 28, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (3, 34, '2026-01-03 15:56:15');
INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES (3, 35, '2026-01-03 15:56:15');
COMMIT;

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID，主键',
  `role_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色标识符（USER, ADMIN, MAINTAINER）',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色描述',
  `is_system_role` tinyint DEFAULT '1' COMMENT '是否系统内置角色：1-是，0-否',
  `status` tinyint DEFAULT '1' COMMENT '角色状态：1-启用，0-禁用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_key` (`role_key`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ----------------------------
-- Records of roles
-- ----------------------------
BEGIN;
INSERT INTO `roles` (`id`, `role_key`, `role_name`, `description`, `is_system_role`, `status`, `created_at`, `updated_at`) VALUES (1, 'USER', '普通用户', '学生/教职工，可认养植物、完成任务、分享知识', 1, 1, '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `roles` (`id`, `role_key`, `role_name`, `description`, `is_system_role`, `status`, `created_at`, `updated_at`) VALUES (2, 'ADMIN', '管理员', '园艺社成员，拥有全系统管理权限', 1, 1, '2026-01-03 15:56:15', '2026-01-03 15:56:15');
INSERT INTO `roles` (`id`, `role_key`, `role_name`, `description`, `is_system_role`, `status`, `created_at`, `updated_at`) VALUES (3, 'MAINTAINER', '养护员', '专业养护人员，负责处理植物异常', 1, 1, '2026-01-03 15:56:15', '2026-01-03 15:56:15');
COMMIT;

-- ----------------------------
-- Table structure for sys_approval
-- ----------------------------
DROP TABLE IF EXISTS `sys_approval`;
CREATE TABLE `sys_approval` (
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批记录表';

-- ----------------------------
-- Records of sys_approval
-- ----------------------------
BEGIN;
INSERT INTO `sys_approval` (`id`, `user_id`, `type`, `original_value`, `new_value`, `reason`, `status`, `approver_id`, `comment`, `created_at`, `updated_at`) VALUES (1, 4, 'REAL_NAME_CHANGE', '张伟', '张炜', '改名了', 'APPROVED', 1, '同意', '2026-02-26 16:29:29', '2026-02-26 16:29:47');
INSERT INTO `sys_approval` (`id`, `user_id`, `type`, `original_value`, `new_value`, `reason`, `status`, `approver_id`, `comment`, `created_at`, `updated_at`) VALUES (2, 7, 'REAL_NAME_CHANGE', '229971004', '何杰', '真实姓名为何杰', 'PENDING', NULL, NULL, '2026-02-26 17:23:58', '2026-02-26 17:23:58');
COMMIT;

-- ----------------------------
-- Table structure for system_parameters
-- ----------------------------
DROP TABLE IF EXISTS `system_parameters`;
CREATE TABLE `system_parameters` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '参数ID，主键',
  `param_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数键名',
  `param_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数值',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '参数描述',
  `updated_by` bigint NOT NULL COMMENT '最后更新人ID',
  `updated_by_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后更新人姓名',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `param_key` (`param_key`),
  KEY `updated_by` (`updated_by`),
  KEY `idx_param_key` (`param_key`),
  CONSTRAINT `system_parameters_ibfk_1` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统参数配置表';

-- ----------------------------
-- Records of system_parameters
-- ----------------------------
BEGIN;
INSERT INTO `system_parameters` (`id`, `param_key`, `param_value`, `description`, `updated_by`, `updated_by_name`, `updated_at`) VALUES (1, 'ADOPTION_PERIOD_MONTHS', '6', '默认认养周期（月数，1学期=6个月）', 1, '张管理员', '2026-01-25 21:34:12');
INSERT INTO `system_parameters` (`id`, `param_key`, `param_value`, `description`, `updated_by`, `updated_by_name`, `updated_at`) VALUES (2, 'TASK_GENERATION_DAYS', '7', '自动任务生成间隔天数', 1, '张管理员', '2026-01-25 21:34:13');
INSERT INTO `system_parameters` (`id`, `param_key`, `param_value`, `description`, `updated_by`, `updated_by_name`, `updated_at`) VALUES (3, 'EVALUATION_START_DATE', '2026-01-01', '成果评比开始日期', 1, '张管理员', '2026-01-25 21:34:13');
INSERT INTO `system_parameters` (`id`, `param_key`, `param_value`, `description`, `updated_by`, `updated_by_name`, `updated_at`) VALUES (4, 'EVALUATION_END_DATE', '2026-06-30', '成果评比结束日期', 1, '张管理员', '2026-01-25 21:34:14');
INSERT INTO `system_parameters` (`id`, `param_key`, `param_value`, `description`, `updated_by`, `updated_by_name`, `updated_at`) VALUES (5, 'MIN_TASK_COMPLETION_RATE', '1.00', '优秀养护人最低任务完成率（1.00=100%）', 1, '张管理员', '2026-01-25 21:34:15');
INSERT INTO `system_parameters` (`id`, `param_key`, `param_value`, `description`, `updated_by`, `updated_by_name`, `updated_at`) VALUES (6, 'ABNORMALITY_REMINDER_HOURS', '48', '异常处理超时提醒小时数', 1, '张管理员', '2026-01-25 21:34:16');
INSERT INTO `system_parameters` (`id`, `param_key`, `param_value`, `description`, `updated_by`, `updated_by_name`, `updated_at`) VALUES (7, 'MAX_ADOPTIONS_PER_USER', '1', '每个用户最多可认养植物数量', 1, '张管理员', '2026-01-25 21:34:16');
INSERT INTO `system_parameters` (`id`, `param_key`, `param_value`, `description`, `updated_by`, `updated_by_name`, `updated_at`) VALUES (8, 'TASK_OVERDUE_DAYS', '3', '任务逾期自动取消认养资格天数', 1, '张管理员', '2026-01-25 21:34:17');
COMMIT;

-- ----------------------------
-- Table structure for task_templates
-- ----------------------------
DROP TABLE IF EXISTS `task_templates`;
CREATE TABLE `task_templates` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务模板ID，主键',
  `plant_species` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '适用植物品种',
  `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务类型（浇水、修剪、施肥等）',
  `task_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务详细描述',
  `frequency_days` int NOT NULL COMMENT '任务生成频率（天数）',
  `duration_minutes` int DEFAULT NULL COMMENT '预计完成时间（分钟）',
  `seasonality` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '季节性（春季、夏季、秋季、冬季、全年）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `operation_requirements` text COLLATE utf8mb4_unicode_ci COMMENT '操作要求',
  `score_standard` text COLLATE utf8mb4_unicode_ci COMMENT '评分标准',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  PRIMARY KEY (`id`),
  KEY `idx_plant_species` (`plant_species`),
  KEY `idx_task_type` (`task_type`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='养护任务模板表';

-- ----------------------------
-- Records of task_templates
-- ----------------------------
BEGIN;
INSERT INTO `task_templates` (`id`, `plant_species`, `task_type`, `task_description`, `frequency_days`, `duration_minutes`, `seasonality`, `created_at`, `updated_at`, `operation_requirements`, `score_standard`, `status`) VALUES (1, 'Prunus serrulata', '浇水', '彻底浇灌樱花树根部，确保土壤湿润但不积水，每次约20升水。', 5, 15, '全年', '2026-01-03 16:00:01', '2026-01-03 16:00:01', NULL, NULL, 1);
INSERT INTO `task_templates` (`id`, `plant_species`, `task_type`, `task_description`, `frequency_days`, `duration_minutes`, `seasonality`, `created_at`, `updated_at`, `operation_requirements`, `score_standard`, `status`) VALUES (2, 'Prunus serrulata', '修剪', '修剪枯枝、病枝和交叉枝，保持树冠通风透光，促进花芽分化。', 90, 60, '冬季', '2026-01-03 16:00:01', '2026-01-03 16:00:01', NULL, NULL, 1);
INSERT INTO `task_templates` (`id`, `plant_species`, `task_type`, `task_description`, `frequency_days`, `duration_minutes`, `seasonality`, `created_at`, `updated_at`, `operation_requirements`, `score_standard`, `status`) VALUES (3, 'Prunus serrulata', '施肥', '施用复合肥，每株约100克，均匀撒在树冠投影范围内。', 60, 20, '春季', '2026-01-03 16:00:01', '2026-01-03 16:00:01', NULL, NULL, 1);
INSERT INTO `task_templates` (`id`, `plant_species`, `task_type`, `task_description`, `frequency_days`, `duration_minutes`, `seasonality`, `created_at`, `updated_at`, `operation_requirements`, `score_standard`, `status`) VALUES (4, 'Ginkgo biloba', '浇水', '银杏树耐旱，仅在连续干旱时浇水，每次约15升。', 10, 10, '夏季', '2026-01-03 16:00:01', '2026-01-03 16:00:01', NULL, NULL, 1);
INSERT INTO `task_templates` (`id`, `plant_species`, `task_type`, `task_description`, `frequency_days`, `duration_minutes`, `seasonality`, `created_at`, `updated_at`, `operation_requirements`, `score_standard`, `status`) VALUES (5, 'Ginkgo biloba', '清理', '清理落叶和杂草，保持树盘清洁。', 30, 25, '秋季', '2026-01-03 16:00:01', '2026-01-03 16:00:01', NULL, NULL, 1);
INSERT INTO `task_templates` (`id`, `plant_species`, `task_type`, `task_description`, `frequency_days`, `duration_minutes`, `seasonality`, `created_at`, `updated_at`, `operation_requirements`, `score_standard`, `status`) VALUES (6, 'Osmanthus fragrans', '浇水', '桂花喜湿润，保持土壤微湿，每次浇水约15升。', 4, 12, '全年', '2026-01-03 16:00:01', '2026-01-03 16:00:01', NULL, NULL, 1);
INSERT INTO `task_templates` (`id`, `plant_species`, `task_type`, `task_description`, `frequency_days`, `duration_minutes`, `seasonality`, `created_at`, `updated_at`, `operation_requirements`, `score_standard`, `status`) VALUES (7, 'Osmanthus fragrans', '施肥', '花期前后施用磷钾肥，促进开花和香气。', 45, 15, '秋季', '2026-01-03 16:00:01', '2026-01-03 16:00:01', NULL, NULL, 1);
INSERT INTO `task_templates` (`id`, `plant_species`, `task_type`, `task_description`, `frequency_days`, `duration_minutes`, `seasonality`, `created_at`, `updated_at`, `operation_requirements`, `score_standard`, `status`) VALUES (8, 'Lagerstroemia indica', '浇水', '紫薇耐旱，但在花期需要充足水分，每次约18升。', 6, 14, '夏季', '2026-01-03 16:00:01', '2026-01-03 16:00:01', NULL, NULL, 1);
INSERT INTO `task_templates` (`id`, `plant_species`, `task_type`, `task_description`, `frequency_days`, `duration_minutes`, `seasonality`, `created_at`, `updated_at`, `operation_requirements`, `score_standard`, `status`) VALUES (9, 'Lagerstroemia indica', '修剪', '花后及时修剪残花，促进二次开花。', 30, 20, '夏季', '2026-01-03 16:00:01', '2026-01-03 16:00:01', NULL, NULL, 1);
INSERT INTO `task_templates` (`id`, `plant_species`, `task_type`, `task_description`, `frequency_days`, `duration_minutes`, `seasonality`, `created_at`, `updated_at`, `operation_requirements`, `score_standard`, `status`) VALUES (10, 'Bambusa multiplex', '浇水', '竹子需水量大，保持土壤湿润，每次浇水约25升。', 3, 18, '全年', '2026-01-03 16:00:01', '2026-01-03 16:00:01', NULL, NULL, 1);
INSERT INTO `task_templates` (`id`, `plant_species`, `task_type`, `task_description`, `frequency_days`, `duration_minutes`, `seasonality`, `created_at`, `updated_at`, `operation_requirements`, `score_standard`, `status`) VALUES (11, 'Bambusa multiplex', '疏笋', '移除过密的新笋，控制竹林蔓延，保持通风。', 15, 40, '春季', '2026-01-03 16:00:01', '2026-01-03 16:00:01', NULL, NULL, 1);
COMMIT;

-- ----------------------------
-- Table structure for user_classes
-- ----------------------------
DROP TABLE IF EXISTS `user_classes`;
CREATE TABLE `user_classes` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  PRIMARY KEY (`user_id`,`class_id`),
  KEY `class_id` (`class_id`),
  CONSTRAINT `user_classes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_classes_ibfk_2` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户班级关联表';

-- ----------------------------
-- Records of user_classes
-- ----------------------------
BEGIN;
INSERT INTO `user_classes` (`user_id`, `class_id`) VALUES (2, 1);
INSERT INTO `user_classes` (`user_id`, `class_id`) VALUES (3, 1);
INSERT INTO `user_classes` (`user_id`, `class_id`) VALUES (4, 2);
COMMIT;

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_roles_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ----------------------------
-- Records of user_roles
-- ----------------------------
BEGIN;
INSERT INTO `user_roles` (`user_id`, `role_id`, `created_at`) VALUES (1, 2, '2026-01-03 15:56:15');
INSERT INTO `user_roles` (`user_id`, `role_id`, `created_at`) VALUES (2, 1, '2026-01-03 15:56:15');
INSERT INTO `user_roles` (`user_id`, `role_id`, `created_at`) VALUES (3, 1, '2026-01-03 15:56:15');
INSERT INTO `user_roles` (`user_id`, `role_id`, `created_at`) VALUES (4, 1, '2026-01-03 15:56:15');
INSERT INTO `user_roles` (`user_id`, `role_id`, `created_at`) VALUES (5, 3, '2026-01-03 15:56:15');
INSERT INTO `user_roles` (`user_id`, `role_id`, `created_at`) VALUES (6, 1, '2026-01-03 15:56:15');
INSERT INTO `user_roles` (`user_id`, `role_id`, `created_at`) VALUES (8, 1, '2026-02-28 17:43:48');
COMMIT;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名，唯一标识',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户密码（加密存储）',
  `real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `student_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学号（学生）或工号（教职工）',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱地址',
  `status` tinyint DEFAULT '1' COMMENT '用户状态：1-启用，0-禁用',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间（软删除）',
  `token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户token',
  `token_expire_time` timestamp NULL DEFAULT NULL COMMENT '用户token过期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- Records of users
-- ----------------------------
BEGIN;
INSERT INTO `users` (`id`, `username`, `password`, `real_name`, `student_id`, `phone`, `email`, `status`, `avatar_url`, `created_at`, `updated_at`, `deleted_at`, `token`, `token_expire_time`) VALUES (1, 'admin', '14e1b600b1fd579f47433b88e8d85291', '张管理员', 'ADMIN001', '13800138001', 'admin@campus.edu.cn', 1, '/api/profile/a4ae2b22-e592-41f0-8914-c801f0f09d43.jpg', '2026-01-03 15:56:15', '2026-02-28 18:56:21', NULL, '364fc04a-0c90-4ef3-bf58-6e8b73014608', '2026-03-07 18:56:21');
INSERT INTO `users` (`id`, `username`, `password`, `real_name`, `student_id`, `phone`, `email`, `status`, `avatar_url`, `created_at`, `updated_at`, `deleted_at`, `token`, `token_expire_time`) VALUES (2, 'student_wang', '14e1b600b1fd579f47433b88e8d85291', '王小明', '2025001', '13800138002', 'wangxiaoming@campus.edu.cn', 1, NULL, '2026-01-03 15:56:15', '2026-02-26 15:55:46', NULL, NULL, NULL);
INSERT INTO `users` (`id`, `username`, `password`, `real_name`, `student_id`, `phone`, `email`, `status`, `avatar_url`, `created_at`, `updated_at`, `deleted_at`, `token`, `token_expire_time`) VALUES (3, 'student_li', '14e1b600b1fd579f47433b88e8d85291', '李小红', '2025002', '13800138003', 'lixiaohong@campus.edu.cn', 1, NULL, '2026-01-03 15:56:15', '2026-02-28 18:25:40', NULL, 'b00bced0-1473-4cae-a847-53b08ac5b61e', '2026-03-07 18:25:40');
INSERT INTO `users` (`id`, `username`, `password`, `real_name`, `student_id`, `phone`, `email`, `status`, `avatar_url`, `created_at`, `updated_at`, `deleted_at`, `token`, `token_expire_time`) VALUES (4, 'zhangwei', '14e1b600b1fd579f47433b88e8d85291', '张炜', '2025003', '13800138004', 'zhangwei@campus.edu.cn', 1, '/api/profile/571e3b41-bc6a-4ab7-a93f-c294d9f44ad1.jpg', '2026-01-03 15:56:15', '2026-02-28 18:57:00', NULL, '78f04781-9d72-429d-ac86-da11ecb9824b', '2026-03-07 18:57:00');
INSERT INTO `users` (`id`, `username`, `password`, `real_name`, `student_id`, `phone`, `email`, `status`, `avatar_url`, `created_at`, `updated_at`, `deleted_at`, `token`, `token_expire_time`) VALUES (5, 'chen', '14e1b600b1fd579f47433b88e8d85291', '陈园艺师', 'MAINT001', '13800138005', 'chenyuanyi@campus.edu.cn', 1, '/api/profile/c2a02523-2c71-4b08-9642-d07a38ed7ff4.jpg', '2026-01-03 15:56:15', '2026-02-27 10:43:47', NULL, '3ef23d68-6cfa-4ca1-811c-d62d620fe7db', '2026-03-06 10:43:47');
INSERT INTO `users` (`id`, `username`, `password`, `real_name`, `student_id`, `phone`, `email`, `status`, `avatar_url`, `created_at`, `updated_at`, `deleted_at`, `token`, `token_expire_time`) VALUES (6, 'teacher_liu', '14e1b600b1fd579f47433b88e8d85291', '刘教授', 'TEACH001', '13800138006', 'liujiaoshou@campus.edu.cn', 1, NULL, '2026-01-03 15:56:15', '2026-02-25 17:24:45', NULL, NULL, NULL);
INSERT INTO `users` (`id`, `username`, `password`, `real_name`, `student_id`, `phone`, `email`, `status`, `avatar_url`, `created_at`, `updated_at`, `deleted_at`, `token`, `token_expire_time`) VALUES (8, '229971005', '14e1b600b1fd579f47433b88e8d85291', '何瑜', NULL, '15923470001', '15923470001@qq.com', 1, NULL, '2026-02-28 17:40:00', '2026-02-28 17:43:48', NULL, NULL, NULL);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
