import React, { useState } from 'react';
import { Row, Col, Card, Statistic, Button, List, Avatar, Tag } from 'antd';
import { 
  CheckCircleOutlined, 
  ClockCircleOutlined, 
  WarningOutlined, 
  EditOutlined, 
  SafetyCertificateOutlined,
  EyeOutlined,
  AppstoreAddOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const UserOverview: React.FC = () => {
  const navigate = useNavigate();
  const role = localStorage.getItem('role') || 'USER';
  const username = localStorage.getItem('username') || 'User';

  // Mock Data
  const stats = {
    pendingTasks: 5,
    completedTasks: 12,
    abnormalities: 2,
    score: 98
  };

  const shortcuts = [
    { key: 'edit-profile', icon: <EditOutlined />, label: '修改信息', path: '/user/profile' },
    { key: 'change-pwd', icon: <SafetyCertificateOutlined />, label: '重置密码', path: '/user/security' },
    { key: 'my-adoptions', icon: <EyeOutlined />, label: '我的认养', path: '/user/my-adoptions', role: 'USER' },
    { key: 'my-audits', icon: <CheckCircleOutlined />, label: '我的审核', path: '/user/admin-audits', role: 'ADMIN' },
    { key: 'my-abnormalities', icon: <WarningOutlined />, label: '我的异常', path: '/user/maintainer-pending', role: 'MAINTAINER' },
  ].filter(item => !item.role || item.role === role);

  return (
    <div>
      <div className="flex items-center mb-8">
        <Avatar size={64} style={{ backgroundColor: '#87d068' }} icon={<UserOutlined />} className="mr-4" />
        <div>
          <h2 className="text-2xl font-bold mb-1">{username}</h2>
          <Tag color={role === 'ADMIN' ? 'red' : role === 'MAINTAINER' ? 'blue' : 'green'}>
            {role === 'ADMIN' ? '管理员' : role === 'MAINTAINER' ? '养护员' : '普通用户'}
          </Tag>
        </div>
      </div>

      <Row gutter={[16, 16]} className="mb-8">
        <Col span={6}>
          <Card>
            <Statistic 
              title="待办业务" 
              value={stats.pendingTasks} 
              prefix={<ClockCircleOutlined />} 
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="已完成" 
              value={stats.completedTasks} 
              prefix={<CheckCircleOutlined />} 
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="相关异常" 
              value={stats.abnormalities} 
              prefix={<WarningOutlined />} 
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="综合评分" 
              value={stats.score} 
              suffix="/ 100" 
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
      </Row>

      <Card title="快捷入口" className="mb-8">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {shortcuts.map(item => (
            <Button 
              key={item.key} 
              size="large" 
              icon={item.icon} 
              onClick={() => navigate(item.path)}
              className="h-20 flex flex-col items-center justify-center gap-2"
            >
              {item.label}
            </Button>
          ))}
          <Button 
            size="large" 
            icon={<AppstoreAddOutlined />} 
            type="dashed"
            className="h-20 flex flex-col items-center justify-center gap-2 text-gray-400"
          >
            自定义
          </Button>
        </div>
      </Card>
    </div>
  );
};

import { UserOutlined } from '@ant-design/icons';
export default UserOverview;
