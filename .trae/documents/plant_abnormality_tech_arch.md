## 1. 架构设计

```mermaid
graph TD
  A[用户浏览器] --> B[React前端应用]
  B --> C[Supabase客户端SDK]
  C --> D[Supabase服务]
  B --> E[WebSocket服务]
  B --> F[Dify图像识别API]
  
  subgraph "前端层"
    B
  end
  
  subgraph "服务层"
    D
    E
    F
  end
```

## 2. 技术描述

- **前端**: React@18 + Ant Design@5 + Tailwind CSS@3 + Vite
- **初始化工具**: vite-init
- **后端服务**: Supabase (认证+数据库+存储)
- **实时通信**: WebSocket (Socket.io)
- **图像识别**: Dify API (https://api.dify.ai/v1)
- **状态管理**: React Context + useReducer
- **路由**: React Router@6

## 3. 路由定义

| 路由 | 用途 | 访问权限 |
|------|------|----------|
| /login | 用户登录页面 | 公共访问 |
| /dashboard | 用户上报主页面 | 普通用户 |
| /history | 用户历史记录 | 普通用户 |
| /admin | 管理员工作台 | 管理员 |
| /admin/kanban | 工单看板 | 管理员 |
| /admin/stats | 统计报表 | 管理员 |
| /maintainer | 养护员任务列表 | 养护员 |
| /maintainer/task/:id | 任务处理详情 | 养护员 |
| /settings | 系统设置 | 管理员 |

## 4. API定义

### 4.1 图像识别API
```
POST https://api.dify.ai/v1/chat-messages
```

请求头：
```
Authorization: Bearer app-PFIR8w3E5fTRmCablD65HEdo
Content-Type: application/json
```

请求体：
```json
{
  "inputs": {},
  "query": "分析植物异常",
  "response_mode": "blocking",
  "conversation_id": "",
  "user": "user_id",
  "files": [{
    "type": "image",
    "transfer_method": "base64",
    "base64": "base64_encoded_image"
  }]
}
```

### 4.2 工单管理API
```
GET /api/tickets
POST /api/tickets
PUT /api/tickets/:id/assign
PUT /api/tickets/:id/status
```

### 4.3 WebSocket事件
```javascript
// 客户端订阅
csocket.on('ticket:assigned', (data) => {})
socket.on('ticket:status_changed', (data) => {})
socket.on('ticket:overdue', (data) => {})

// 客户端发送
socket.emit('ticket:update_status', {ticketId, status})
```

## 5. 服务器架构

```mermaid
graph TD
  A[客户端请求] --> B[API网关]
  B --> C[认证中间件]
  C --> D[业务逻辑层]
  D --> E[数据库访问层]
  E --> F[Supabase数据库]
  
  D --> G[WebSocket服务]
  G --> H[消息队列]
  H --> I[通知服务]
  
  subgraph "后端服务层"
    B
    C
    D
    E
    G
    H
    I
  end
```

## 6. 数据模型

### 6.1 实体关系图
```mermaid
erDiagram
  USER ||--o{ TICKET : creates
  USER ||--o{ ASSIGNMENT : assigned_to
  TICKET ||--o{ ASSIGNMENT : has
  TICKET ||--o{ ATTACHMENT : contains
  USER ||--o{ AUDIT_LOG : generates
  
  USER {
    uuid id PK
    string email UK
    string password_hash
    string name
    string role
    timestamp created_at
    timestamp updated_at
  }
  
  TICKET {
    uuid id PK
    uuid user_id FK
    string title
    text description
    string abnormality_type
    string status
    string priority
    string location
    timestamp reported_at
    timestamp assigned_at
    timestamp completed_at
    timestamp created_at
  }
  
  ASSIGNMENT {
    uuid id PK
    uuid ticket_id FK
    uuid maintainer_id FK
    timestamp assigned_at
    timestamp due_at
    timestamp completed_at
    string notes
  }
  
  ATTACHMENT {
    uuid id PK
    uuid ticket_id FK
    string file_name
    string file_path
    string file_type
    int file_size
    boolean is_before
    timestamp created_at
  }
  
  AUDIT_LOG {
    uuid id PK
    uuid user_id FK
    string action
    string resource_type
    uuid resource_id
    json old_values
    json new_values
    string ip_address
    timestamp created_at
  }
```

### 6.2 数据定义语言

```sql
-- 用户表
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  name VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL CHECK (role IN ('user', 'maintainer', 'admin')),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 工单表
CREATE TABLE tickets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  title VARCHAR(200) NOT NULL,
  description TEXT NOT NULL,
  abnormality_type VARCHAR(50) NOT NULL,
  status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'assigned', 'processing', 'completed')),
  priority VARCHAR(10) DEFAULT 'normal' CHECK (priority IN ('low', 'normal', 'high', 'urgent')),
  location VARCHAR(100),
  reported_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  assigned_at TIMESTAMP WITH TIME ZONE,
  completed_at TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 分派记录表
CREATE TABLE assignments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  ticket_id UUID REFERENCES tickets(id) ON DELETE CASCADE,
  maintainer_id UUID REFERENCES users(id) ON DELETE CASCADE,
  assigned_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  due_at TIMESTAMP WITH TIME ZONE,
  completed_at TIMESTAMP WITH TIME ZONE,
  notes TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 附件表
CREATE TABLE attachments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  ticket_id UUID REFERENCES tickets(id) ON DELETE CASCADE,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(500) NOT NULL,
  file_type VARCHAR(50) NOT NULL,
  file_size INT NOT NULL,
  is_before BOOLEAN DEFAULT true,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 审计日志表
CREATE TABLE audit_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES users(id) ON DELETE SET NULL,
  action VARCHAR(100) NOT NULL,
  resource_type VARCHAR(50) NOT NULL,
  resource_id UUID,
  old_values JSONB,
  new_values JSONB,
  ip_address INET,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 索引创建
CREATE INDEX idx_tickets_user_id ON tickets(user_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_priority ON tickets(priority);
CREATE INDEX idx_tickets_created_at ON tickets(created_at DESC);
CREATE INDEX idx_assignments_maintainer_id ON assignments(maintainer_id);
CREATE INDEX idx_assignments_ticket_id ON assignments(ticket_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);

-- Supabase RLS策略
ALTER TABLE tickets ENABLE ROW LEVEL SECURITY;
ALTER TABLE assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE attachments ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- 基本权限设置
GRANT SELECT ON tickets TO anon;
GRANT ALL ON tickets TO authenticated;
GRANT SELECT ON assignments TO anon;
GRANT ALL ON assignments TO authenticated;
GRANT SELECT ON attachments TO anon;
GRANT ALL ON attachments TO authenticated;
GRANT SELECT ON audit_logs TO authenticated;
```

## 7. 性能优化

- **图片压缩**: 前端上传前压缩至1080p，减少传输时间
- **缓存策略**: 使用React Query缓存频繁请求的数据
- **分页加载**: 工单列表采用虚拟滚动，每页20条
- **CDN加速**: 静态资源使用CDN分发
- **数据库优化**: 关键字段建立索引，查询使用预加载

## 8. 安全考虑

- **认证安全**: JWT令牌+刷新令牌机制，7天过期
- **权限控制**: 前端路由守卫+后端接口权限验证
- **数据加密**: 敏感数据使用AES加密存储
- **文件安全**: 上传文件类型白名单，病毒扫描
- **审计追踪**: 所有操作记录完整审计日志
- **HTTPS**: 全站HTTPS加密传输