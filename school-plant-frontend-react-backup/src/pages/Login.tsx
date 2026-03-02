import React, { useState } from "react";
import {
  Form,
  Input,
  Button,
  Card,
  Select,
  message,
  Tabs,
  ConfigProvider,
  theme,
} from "antd";
import {
  UserOutlined,
  LockOutlined,
  MailOutlined,
  SafetyOutlined,
  PhoneOutlined,
} from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { login, register } from "@/api/auth";
import CryptoJS from "crypto-js";

const { Option } = Select;

const Login: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState("login");
  const [form] = Form.useForm();

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      // MD5 Encryption for password
      const encryptedValues = { ...values };
      if (values.password) {
        encryptedValues.password = CryptoJS.MD5(values.password).toString();
      }
      if (values.confirmPassword) {
        encryptedValues.confirmPassword = CryptoJS.MD5(
          values.confirmPassword,
        ).toString();
      }

      if (activeTab === "login") {
        const res: any = await login(encryptedValues);
        localStorage.setItem("token", res.tokenInfo.tokenValue);
        localStorage.setItem("role", res.roleType);
        message.success("登录成功");
        // Redirect to new User Center Overview
        navigate("/user/overview");
      } else {
        await register(encryptedValues);
        message.success("注册成功，请登录");
        setActiveTab("login");
        form.resetFields();
      }
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: "#52c41a", // Plant Green
          borderRadius: 8,
        },
      }}
    >
      <div
        className="min-h-screen flex items-center justify-center bg-cover bg-center bg-no-repeat"
        style={{
          backgroundImage:
            'url("https://images.unsplash.com/photo-1518531933037-9a8476317d1d?ixlib=rb-1.2.1&auto=format&fit=crop&w=1950&q=80")',
          backgroundColor: "#f0f2f5",
        }}
      >
        {/* Overlay for better readability */}
        <div className="absolute inset-0 bg-black bg-opacity-30 backdrop-blur-sm"></div>

        <div className="relative z-10 w-full max-w-4xl flex shadow-2xl rounded-2xl overflow-hidden bg-white/90 backdrop-blur-md m-4">
          {/* Left Side: Branding / Image */}
          <div className="hidden md:flex md:w-1/2 bg-gradient-to-br from-green-600 to-teal-700 p-10 flex-col justify-between text-white relative overflow-hidden">
            <div className="absolute top-0 left-0 w-full h-full opacity-20 bg-[url('https://www.transparenttextures.com/patterns/leaf.png')]"></div>
            <div>
              <h1 className="text-4xl font-bold mb-4">
                校园植物
                <br />
                认养与养护
              </h1>
              <p className="text-green-100 text-lg">
                致力于打造绿色校园，
                <br />
                让每一株植物都有专属守护者。
              </p>
            </div>
            <div className="text-sm opacity-80">
              © {new Date().getFullYear()} School Plant System
            </div>
          </div>

          {/* Right Side: Form */}
          <div className="w-full md:w-1/2 p-8 md:p-12 bg-white">
            <div className="text-center mb-8 md:hidden">
              <h2 className="text-2xl font-bold text-green-600">
                校园植物认养与养护
              </h2>
            </div>

            <Tabs
              activeKey={activeTab}
              onChange={setActiveTab}
              centered
              size="large"
              className="mb-6"
              items={[
                { label: "账号登录", key: "login" },
                { label: "新用户注册", key: "register" },
              ]}
            />

            <Form
              form={form}
              name="login_form"
              layout="vertical"
              initialValues={{ remember: true, role: "USER" }}
              onFinish={onFinish}
              size="large"
            >
              <Form.Item
                name="username"
                rules={[{ required: true, message: "请输入用户名!" }]}
              >
                <Input
                  prefix={<UserOutlined className="text-gray-400" />}
                  placeholder="用户名"
                />
              </Form.Item>

              {activeTab === "register" && (
                <>
                  <Form.Item
                    name="phone"
                    rules={[{ required: true, message: "请输入手机号!" }]}
                  >
                    <Input
                      prefix={<PhoneOutlined className="text-gray-400" />}
                      placeholder="手机号"
                    />
                  </Form.Item>
                  <Form.Item
                    name="email"
                    rules={[
                      { required: true, message: "请输入邮箱!" },
                      { type: "email", message: "邮箱格式不正确!" },
                    ]}
                  >
                    <Input
                      prefix={<MailOutlined className="text-gray-400" />}
                      placeholder="邮箱"
                    />
                  </Form.Item>
                </>
              )}

              <Form.Item
                name="password"
                rules={[{ required: true, message: "请输入密码!" }]}
              >
                <Input.Password
                  prefix={<LockOutlined className="text-gray-400" />}
                  placeholder="密码"
                />
              </Form.Item>

              {activeTab === "register" && (
                <>
                  <Form.Item
                    name="confirmPassword"
                    dependencies={["password"]}
                    rules={[
                      { required: true, message: "请确认密码!" },
                      ({ getFieldValue }) => ({
                        validator(_, value) {
                          if (!value || getFieldValue("password") === value) {
                            return Promise.resolve();
                          }
                          return Promise.reject(
                            new Error("两次输入的密码不一致!"),
                          );
                        },
                      }),
                    ]}
                  >
                    <Input.Password
                      prefix={<SafetyOutlined className="text-gray-400" />}
                      placeholder="确认密码"
                    />
                  </Form.Item>
                  <Form.Item name="role" label="注册角色">
                    <Select>
                      <Option value="USER">普通用户 (学生/老师)</Option>
                      {/* Usually maintainers are created by admin, but keeping for demo */}
                      <Option value="MAINTAINER">校园养护员</Option>
                    </Select>
                  </Form.Item>
                </>
              )}

              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  className="w-full h-12 text-lg font-medium shadow-md hover:shadow-lg transition-all"
                  loading={loading}
                >
                  {activeTab === "login" ? "立即登录" : "立即注册"}
                </Button>
              </Form.Item>
            </Form>
          </div>
        </div>
      </div>
    </ConfigProvider>
  );
};

export default Login;
