# 角色管理模块操作手册

## 1. 数据库变更说明
本模块新增了角色状态管理功能，需对数据库 `roles` 表进行结构变更。请在数据库中执行以下 SQL 语句：

```sql
ALTER TABLE `roles` ADD COLUMN `status` tinyint DEFAULT 1 COMMENT '角色状态：1-启用，0-禁用';
UPDATE `roles` SET `status` = 1;
```

## 2. 接口功能说明

### 2.1 获取角色列表
- **接口**: `GET /system/role/list`
- **功能**: 分页查询角色列表，支持模糊搜索（名称、描述）和状态筛选。
- **参数**:
  - `page`: 页码（默认1）
  - `size`: 每页数量（默认10）
  - `roleName`: 角色名称（模糊匹配）
  - `description`: 描述（模糊匹配）
  - `status`: 状态（1-启用，0-禁用）
- **权限**: `system:role:manage`

### 2.2 获取角色详情
- **接口**: `GET /system/role/{id}`
- **功能**: 获取单个角色的详细信息，包括已关联的权限ID列表。
- **权限**: `system:role:manage`

### 2.3 新增角色
- **接口**: `POST /system/role`
- **功能**: 创建新角色并分配权限。
- **注意事项**:
  - 角色名称和角色编码必须唯一。
  - `permissionIds` 为权限ID列表。
- **权限**: `system:role:manage`

### 2.4 修改角色
- **接口**: `PUT /system/role`
- **功能**: 更新角色基本信息及权限配置。
- **注意事项**:
  - 系统内置角色（`is_system_role=1`）禁止修改角色编码。
- **权限**: `system:role:manage`

### 2.5 修改角色状态
- **接口**: `PUT /system/role/status`
- **功能**: 启用或禁用角色。
- **影响**: 禁用后，关联该角色的用户将立即失去相关权限（需重新登录或等待缓存失效）。
- **权限**: `system:role:manage`

### 2.6 删除角色
- **接口**: `DELETE /system/role/{ids}`
- **功能**: 批量删除角色。
- **限制**:
  - 系统内置角色禁止删除。
  - 已分配给用户的角色禁止删除（需先取消关联）。
- **权限**: `system:role:manage`

### 2.7 导出角色
- **接口**: `GET /system/role/export`
- **功能**: 导出符合查询条件的角色数据为 Excel 文件。
- **权限**: `system:role:manage`

## 3. 单元测试覆盖
- 已编写 `RoleServiceImplTest` 覆盖核心业务逻辑，包括新增、更新、删除及唯一性校验。
