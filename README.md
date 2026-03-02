# School Plant Backend

校园植物认养与养护系统后端服务 (Spring Boot + MyBatis-Plus + Sa-Token + Quartz)

## 1. 环境准备
*   JDK 17+
*   MySQL 9.0
*   Redis 7.x
*   Maven 3.8+

## 2. 快速启动
1.  **创建数据库**:
    执行 `schema.sql` (此前已生成) 初始化数据库表结构。
    
2.  **配置数据库连接**:
    修改 `src/main/resources/application.yml` 中的 `spring.datasource.url`, `username`, `password` 以及 `spring.data.redis` 配置。

3.  **运行项目**:
    运行 `com.schoolplant.SchoolPlantApplication` (需自行创建启动类)。

## 3. 核心功能实现说明

### 3.1 权限控制 (Sa-Token)
*   **普通用户**: 仅可访问 `/adoption/**`, `/task/**` 等个人相关接口。
*   **管理员**: 拥有 `/plant/import`, `/plant/export` 等高级权限。
*   **后勤**: 可访问 `/health/resolve` 处理异常。
*   **实现**: 使用 `@SaCheckRole` 和 `@SaCheckLogin` 注解进行拦截。

### 3.2 定时任务 (Quartz)
*   **每日任务生成**: `TaskGenerationJob` 每天凌晨 4:00 运行，调用 `CareTaskService.generateDailyTasks()`。
*   **逾期检查**: `TaskTimeoutJob` 每天凌晨 2:00 运行，检查超期3天未完成任务，自动取消认养资格。
*   **配置**: 见 `QuartzConfig.java`。

### 3.3 数据导入导出 (EasyExcel)
*   **导出**: `/plant/export` 接口，流式写入 Excel。
*   **导入**: `/plant/import` 接口，使用 Listener 模式批量读取并保存。

### 3.4 异常上报闭环
*   用户提交 -> 状态 `PENDING` -> 后勤处理 -> 状态 `RESOLVED`。
*   **超时告警**: `HealthServiceImpl.checkTimeoutReports()` 检查 48小时未处理工单。

## 4. 接口测试用例 (Postman Logic)

### 4.1 登录
*   **URL**: `POST /api/auth/login`
*   **Params**: `username=admin`, `password=123456`
*   **Response**: `{ "code": 200, "data": { "tokenValue": "..." } }`

### 4.2 申请认养
*   **Header**: `satoken: {tokenValue}`
*   **URL**: `POST /api/adoption/apply`
*   **Params**: `plantId=1`
*   **Response**: `{ "code": 200, "msg": "操作成功" }` (若重复申请会报错)

### 4.3 任务打卡
*   **URL**: `POST /api/task/complete`
*   **Body**: `{ "id": 101, "imageUrl": "http://oss..." }`

### 4.4 导出植物
*   **URL**: `GET /api/plant/export`
*   **Response**: 文件流下载

## 5. 工程结构
*   `common`: 通用结果集 R, 全局异常处理
*   `config`: 配置类 (MyBatis, Quartz, Web)
*   `controller`: 接口层
*   `entity`: 实体类
*   `mapper`: DAO层
*   `service`: 业务逻辑层 (核心)
*   `job`: 定时任务实现
