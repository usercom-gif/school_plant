import React, { useState } from 'react';
import { Form, Input, Button, Card, message, Upload, Avatar, Tabs } from 'antd';
import { UserOutlined, UploadOutlined } from '@ant-design/icons';
import UserProfileCard from './ProfileCard';

const UserProfile: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  // Mock initial values
  const initialValues = {
    realName: localStorage.getItem('username') || '用户',
    phone: '13800138000',
    email: 'user@example.com',
    bio: '热爱植物，热爱生活'
  };

  const onFinish = (values: any) => {
    setLoading(true);
    // Mock API call
    setTimeout(() => {
      setLoading(false);
      message.success('基础信息修改成功！');
      console.log('Updated:', values);
    }, 1000);
  };

  return (
    <div className="max-w-3xl mx-auto">
      <Tabs defaultActiveKey="1" items={[
        {
          key: '1',
          label: '个人信息卡片',
          children: <UserProfileCard />
        },
        {
          key: '2',
          label: '编辑资料',
          children: (
            <Card title="编辑基础信息" bordered={false} className="shadow-sm">
              <div className="flex justify-center mb-8">
                <div className="text-center">
                  <Avatar size={100} icon={<UserOutlined />} className="mb-4 bg-gray-200" />
                  <Upload showUploadList={false}>
                    <Button icon={<UploadOutlined />}>更换头像</Button>
                  </Upload>
                </div>
              </div>

              <Form
                form={form}
                layout="vertical"
                initialValues={initialValues}
                onFinish={onFinish}
              >
                <Form.Item
                  name="realName"
                  label="真实姓名"
                  rules={[{ required: true, message: '请输入真实姓名' }]}
                >
                  <Input size="large" />
                </Form.Item>

                <Form.Item
                  name="phone"
                  label="联系电话"
                  rules={[{ required: true, message: '请输入联系电话' }]}
                >
                  <Input size="large" />
                </Form.Item>

                <Form.Item
                  name="email"
                  label="电子邮箱"
                  rules={[{ type: 'email', message: '请输入有效的邮箱地址' }]}
                >
                  <Input size="large" />
                </Form.Item>

                <Form.Item
                  name="bio"
                  label="个人简介"
                >
                  <Input.TextArea rows={4} showCount maxLength={200} />
                </Form.Item>

                <Form.Item>
                  <Button type="primary" htmlType="submit" size="large" loading={loading} block>
                    保存修改
                  </Button>
                </Form.Item>
              </Form>
            </Card>
          )
        }
      ]} />
    </div>
  );
};

export default UserProfile;
