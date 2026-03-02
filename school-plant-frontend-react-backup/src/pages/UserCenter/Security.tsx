import React, { useState } from 'react';
import { Form, Input, Button, Card, message, Alert } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import CryptoJS from 'crypto-js';
import { useNavigate } from 'react-router-dom';

const UserSecurity: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  const onFinish = (values: any) => {
    setLoading(true);
    
    // Simulate API call
    setTimeout(() => {
      // In real implementation:
      // const encryptedOld = CryptoJS.MD5(values.oldPassword).toString();
      // const encryptedNew = CryptoJS.MD5(values.newPassword).toString();
      // await api.changePassword({ old: encryptedOld, new: encryptedNew });

      setLoading(false);
      message.success('密码修改成功，请重新登录');
      localStorage.clear();
      navigate('/login');
    }, 1500);
  };

  return (
    <div className="max-w-md mx-auto mt-8">
      <Card title="修改密码" bordered={false}>
        <Alert
          message="安全提示"
          description="建议定期更换密码，密码应包含字母、数字和特殊字符。"
          type="info"
          showIcon
          className="mb-6"
        />

        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
        >
          <Form.Item
            name="oldPassword"
            label="原密码"
            rules={[{ required: true, message: '请输入原密码' }]}
          >
            <Input.Password prefix={<LockOutlined />} size="large" placeholder="请输入当前使用的密码" />
          </Form.Item>

          <Form.Item
            name="newPassword"
            label="新密码"
            rules={[
              { required: true, message: '请输入新密码' },
              { min: 6, message: '密码长度不能少于6位' }
            ]}
          >
            <Input.Password prefix={<LockOutlined />} size="large" placeholder="请输入新密码" />
          </Form.Item>

          <Form.Item
            name="confirmNewPassword"
            label="确认新密码"
            dependencies={['newPassword']}
            rules={[
              { required: true, message: '请确认新密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次输入的密码不一致!'));
                },
              }),
            ]}
          >
            <Input.Password prefix={<LockOutlined />} size="large" placeholder="请再次输入新密码" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" size="large" loading={loading} block danger>
              确认重置密码
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default UserSecurity;
