import React, { useState, useEffect } from 'react';
import { Layout, Menu, Avatar, Dropdown, Button, Breadcrumb, Typography } from 'antd';
import { 
  UserOutlined, 
  HomeOutlined, 
  AppstoreOutlined, 
  SafetyCertificateOutlined, 
  LogoutOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  FormOutlined,
  CalendarOutlined,
  WarningOutlined,
  TrophyOutlined,
  ShareAltOutlined,
  FileTextOutlined,
  CheckSquareOutlined,
  AuditOutlined,
  BarChartOutlined
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation, Link } from 'react-router-dom';

const { Header, Sider, Content } = Layout;
const { Title } = Typography;

const UserCenterLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const role = localStorage.getItem('role') || 'USER'; // USER, ADMIN, MAINTAINER
  const username = localStorage.getItem('username') || '用户';

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  const userMenu = (
    <Menu>
      <Menu.Item key="logout" icon={<LogoutOutlined />} onClick={handleLogout}>
        退出登录
      </Menu.Item>
    </Menu>
  );

  // Dynamic Menu Items based on Role
  const getMenuItems = () => {
    const commonItems = [
      {
        key: '/user/overview',
        icon: <HomeOutlined />,
        label: '个人中心首页',
      },
      {
        key: '/user/profile',
        icon: <UserOutlined />,
        label: '基础信息管理',
      },
      {
        key: '/user/security',
        icon: <SafetyCertificateOutlined />,
        label: '密码修改',
      },
    ];

    let roleItems: any[] = [];

    if (role === 'USER') {
      roleItems = [
        {
          key: 'user-exclusive',
          label: '我的认养与任务',
          type: 'group',
          children: [
            { key: '/user/my-adoptions', icon: <AppstoreOutlined />, label: '我的认养' },
            { key: '/user/my-tasks', icon: <CalendarOutlined />, label: '我的养护任务' },
            { key: '/user/my-abnormalities', icon: <WarningOutlined />, label: '我的异常上报' },
            { key: '/user/my-achievements', icon: <TrophyOutlined />, label: '我的认养成果' },
            { key: '/user/my-knowledge', icon: <ShareAltOutlined />, label: '我的知识共享' },
            { key: '/user/my-applications', icon: <FormOutlined />, label: '我的申请记录' },
          ]
        }
      ];
    } else if (role === 'ADMIN') {
      roleItems = [
        {
          key: 'admin-exclusive',
          label: '管理员工作台',
          type: 'group',
          children: [
            { key: '/user/admin-logs', icon: <FileTextOutlined />, label: '我的操作日志' },
            { key: '/user/admin-audits', icon: <CheckSquareOutlined />, label: '我的审核任务' },
            { key: '/user/admin-abnormalities', icon: <WarningOutlined />, label: '我的分派异常' },
            { key: '/user/admin-evaluations', icon: <AuditOutlined />, label: '我的评比操作' },
          ]
        }
      ];
    } else if (role === 'MAINTAINER') {
      roleItems = [
        {
          key: 'maintainer-exclusive',
          label: '养护员工作台',
          type: 'group',
          children: [
            { key: '/user/maintainer-pending', icon: <WarningOutlined />, label: '我的待处理异常' },
            { key: '/user/maintainer-records', icon: <BarChartOutlined />, label: '我的异常处理记录' },
            { key: '/user/maintainer-tracking', icon: <AppstoreOutlined />, label: '我的养护跟踪' },
          ]
        }
      ];
    }

    return [...commonItems, ...roleItems];
  };

  // Breadcrumb mapping
  const breadcrumbNameMap: Record<string, string> = {
    '/user': '个人中心',
    '/user/overview': '首页',
    '/user/profile': '基础信息',
    '/user/security': '密码修改',
    '/user/my-adoptions': '我的认养',
    '/user/my-tasks': '我的养护任务',
    '/user/my-abnormalities': '我的异常上报',
    '/user/my-achievements': '我的认养成果',
    '/user/my-knowledge': '我的知识共享',
    '/user/my-applications': '我的申请记录',
    '/user/admin-logs': '操作日志',
    '/user/admin-audits': '审核任务',
    '/user/admin-abnormalities': '分派异常',
    '/user/admin-evaluations': '评比操作',
    '/user/maintainer-pending': '待处理异常',
    '/user/maintainer-records': '处理记录',
    '/user/maintainer-tracking': '养护跟踪',
  };

  const pathSnippets = location.pathname.split('/').filter(i => i);
  const breadcrumbItems = pathSnippets.map((_, index) => {
    const url = `/${pathSnippets.slice(0, index + 1).join('/')}`;
    return {
      title: breadcrumbNameMap[url] || url,
    };
  });

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed} theme="light" className="shadow-md z-10">
        <div className="h-16 flex items-center justify-center border-b border-gray-100">
          <div className={`font-bold text-green-600 transition-all duration-300 ${collapsed ? 'text-xl' : 'text-lg'}`}>
            {collapsed ? 'SP' : 'School Plant'}
          </div>
        </div>
        <Menu
          theme="light"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={getMenuItems()}
          onClick={({ key }) => navigate(key)}
          className="border-r-0"
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: 0 }} className="flex justify-between items-center px-6 shadow-sm z-10">
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{ fontSize: '16px', width: 64, height: 64 }}
          />
          <div className="flex items-center gap-4">
            <span className="text-gray-500">欢迎回来, {role === 'ADMIN' ? '管理员' : role === 'MAINTAINER' ? '养护员' : '同学'}</span>
            <Dropdown overlay={userMenu} placement="bottomRight">
              <div className="flex items-center gap-2 cursor-pointer hover:bg-gray-50 px-3 py-1 rounded-full transition-colors">
                <Avatar style={{ backgroundColor: '#87d068' }} icon={<UserOutlined />} />
                <span className="font-medium text-gray-700">{username}</span>
              </div>
            </Dropdown>
          </div>
        </Header>
        <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280, background: '#f0f2f5' }}>
          <div className="mb-4">
             <Breadcrumb items={breadcrumbItems} />
          </div>
          <div className="bg-white p-6 rounded-lg shadow-sm min-h-full">
            <React.Suspense fallback={<div className="p-10 text-center">Loading...</div>}>
              <Outlet />
            </React.Suspense>
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default UserCenterLayout;
