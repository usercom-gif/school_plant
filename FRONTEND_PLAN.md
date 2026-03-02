# 校园植物异常系统前端规划文档

## 1. 技术栈

* **框架**: React 18.2.0

* **构建工具**: Vite 4.4.0

* **UI组件库**: Ant Design 5.x

* **样式方案**: Tailwind CSS 3.3.0

* **状态管理**: React Context + useReducer

* **路由**: React Router 6.x

* **HTTP客户端**: Axios 1.4.0

* **WebSocket**: Socket.io-client 4.6.0

* **图片处理**: Compressor.js + React Image Crop

## 2. 页面架构

### 2.1 认证相关页面

* **登录页面** (`/login`)

  * 邮箱密码登录表单

  * 角色选择下拉框（用户/养护员/管理员）

  * 记住密码选项

  * 忘记密码链接

### 2.2 用户端页面

* **用户仪表板** (`/dashboard`)

  * 异常上报表单区域

  * 图片上传组件（支持拖拽）

  * 异常类型选择器

  * 描述文本输入框（最少20字计数器）

  * 智能识别结果展示卡片

  * 解决方案推荐列表

* **历史记录页面** (`/history`)

  * 个人提交记录列表

  * 状态筛选标签页（全部/处理中/已完成）

  * 记录卡片（显示缩略图、标题、状态、时间）

  * 详情查看弹窗

### 2.3 管理端页面

* **管理员工作台** (`/admin`)

  * 工单统计卡片（待处理/处理中/已完成数量）

  * 快捷操作按钮组

  * 最近活动时间线

* **工单看板页面** (`/admin/kanban`)

  * 看板式任务面板（待分派/处理中/已完成）

  * 工单卡片（标题、地点、紧急程度、上报时间）

  * 拖拽分派功能

  * 筛选器（时间范围、区域、紧急程度）

  * 批量操作工具栏

* **工单分派页面** (`/admin/assign/:id`)

  * 工单详情展示

  * 养护员选择器

  * 优先级设置

  * 备注说明输入

  * 分派确认按钮

* **统计报表页面** (`/admin/stats`)

  * 处理效率图表（折线图）

  * 异常类型分布（饼图）

  * 区域热力图

  * 养护员绩效排名

  * 导出报表功能

### 2.4 养护员端页面

* **任务列表页面** (`/maintainer`)

  * 移动端优化设计

  * 任务卡片列表（大图模式）

  * 紧急程度标识（颜色编码）

  * 倒计时显示（48小时超时提醒）

  * 滑动操作菜单（接受/拒绝）

* **任务处理页面** (`/maintainer/task/:id`)

  * 任务详情展示

  * 处理前照片对比

  * 相机拍照上传功能

  * 处理方法选择器

  * 使用材料输入框

  * 处理效果评估（星级评分）

  * 提交完成按钮

## 3. 核心功能组件

### 3.1 图片上传组件

```typescript
interface ImageUploadProps {
  maxSize?: number; // 默认5MB
  accept?: string; // 默认'.jpg,.png'
  multiple?: boolean; // 默认false
  onChange: (files: File[]) => void;
  onPreview: (file: File) => void;
}
```

### 3.2 WebSocket连接管理

```typescript
interface WebSocketService {
  connect: () => void;
  disconnect: () => void;
  subscribe: (event: string, callback: Function) => void;
  emit: (event: string, data: any) => void;
}
```

### 3.3 角色权限控制

```typescript
interface AuthContextType {
  user: User | null;
  role: 'user' | 'maintainer' | 'admin' | null;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
  hasPermission: (permission: string) => boolean;
}
```

## 4. 移动端适配方案

### 4.1 养护员界面（移动端优先）

* **视口配置**: `width=device-width, initial-scale=1.0`

* **触摸优化**: 最小点击区域44px×44px

* **手势支持**: 左滑显示操作菜单

* **相机集成**: 调用原生相机API

* **离线支持**: 本地缓存未提交数据

### 4.2 响应式断点

```css
/* 超小屏幕（手机，小于768px） */
@media (max-width: 767px) { ... }

/* 小屏幕（平板，768px及以上） */
@media (min-width: 768px) and (max-width: 991px) { ... }

/* 中等屏幕（桌面，992px及以上） */
@media (min-width: 992px) and (max-width: 1199px) { ... }

/* 大屏幕（大桌面，1200px及以上） */
@media (min-width: 1200px) { ... }
```

## 5. 状态管理结构

```typescript
interface AppState {
  auth: {
    user: User | null;
    token: string | null;
    role: UserRole | null;
  };
  tickets: {
    list: Ticket[];
    loading: boolean;
    filters: FilterOptions;
  };
  websocket: {
    connected: boolean;
    notifications: Notification[];
  };
}
```

## 6. 关键功能实现

### 6.1 图片压缩上传

```typescript
const compressAndUpload = async (file: File): Promise<string> => {
  // 使用Compressor.js压缩
  const compressedFile = await new Compressor(file, {
    quality: 0.8,
    maxWidth: 1920,
    maxHeight: 1080,
  });
  
  // 上传到Supabase存储
  const { data } = await supabase.storage
    .from('plant-images')
    .upload(`tickets/${Date.now()}-${file.name}`, compressedFile);
    
  return data.path;
};
```

### 6.2 实时通知处理

```typescript
useEffect(() => {
  const socket = io(WS_URL);
  
  socket.on('ticket:assigned', (data) => {
    if (data.maintainerId === currentUser.id) {
      notification.success({
        message: '新任务分派',
        description: `您有新的植物异常处理任务`,
      });
      refetchTasks();
    }
  });
  
  return () => socket.disconnect();
}, [currentUser.id]);
```

### 6.3 权限路由守卫

```typescript
const PrivateRoute = ({ children, requiredRole }: PrivateRouteProps) => {
  const { user, role } = useAuth();
  
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  
  if (requiredRole && role !== requiredRole) {
    return <Navigate to="/unauthorized" replace />;
  }
  
  return children;
};
```

## 7. 性能优化策略

### 7.1 代码分割

```typescript
// 路由懒加载
const AdminDashboard = lazy(() => import('./pages/AdminDashboard'));
const MaintainerView = lazy(() => import('./pages/MaintainerView'));
```

### 7.2 图片优化

* WebP格式优先，JPEG/PNG回退

* 响应式图片srcset

* 懒加载Intersection Observer

* CDN缓存策略

### 7.3 数据缓存

* React Query缓存策略

* 本地存储重要数据

* 增量更新机制

## 8. 错误处理

### 8.1 全局错误边界

```typescript
class ErrorBoundary extends React.Component<Props, State> {
  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    // 记录到错误日志服务
    logError(error, errorInfo);
  }
  
  render() {
    if (this.state.hasError) {
      return <ErrorFallback error={this.state.error} />;
    }
    
    return this.props.children;
  }
}
```

### 8.2 API错误处理

```typescript
const handleApiError = (error: AxiosError) => {
  if (error.response?.status === 401) {
    // 未授权，跳转登录
    navigate('/login');
  } else if (error.response?.status === 403) {
    // 权限不足
    message.error('您没有权限执行此操作');
  } else {
    // 通用错误提示
    message.error('操作失败，请稍后重试');
  }
};
```

## 9. 测试策略

### 9.1 单元测试

* Jest + React Testing Library

* 组件渲染测试

* 业务逻辑测试

* 工具函数测试

### 9.2 集成测试

* API接口测试

* WebSocket连接测试

* 权限控制测试

* 端到端用户流程测试

### 9.3 性能测试

* 页面加载时间监控

* 图片上传速度测试

* 实时通知延迟测试

* 移动端性能基准

