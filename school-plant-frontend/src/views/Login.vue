<template>
  <div
    class="min-h-screen flex items-center justify-center bg-cover bg-center bg-no-repeat"
    style="
      background-image: url(&quot;https://images.unsplash.com/photo-1518531933037-9a8476317d1d?ixlib=rb-1.2.1&auto=format&fit=crop&w=1950&q=80&quot;);
      background-color: #f0f2f5;
    "
  >
    <!-- Overlay -->
    <div class="absolute inset-0 bg-black bg-opacity-30 backdrop-blur-sm"></div>

    <div
      class="relative z-10 w-full max-w-4xl flex shadow-2xl rounded-2xl overflow-hidden bg-white/90 backdrop-blur-md m-4"
    >
      <!-- Left Side -->
      <div
        class="hidden md:flex md:w-1/2 bg-gradient-to-br from-green-600 to-teal-700 p-10 flex-col justify-between text-white relative overflow-hidden"
      >
        <div
          class="absolute top-0 left-0 w-full h-full opacity-20"
          style="
            background-image: url(&quot;https://www.transparenttextures.com/patterns/leaf.png&quot;);
          "
        ></div>
        <div>
          <h1 class="text-4xl font-bold mb-4">校园植物<br />认养与养护</h1>
          <p class="text-green-100 text-lg">
            致力于打造绿色校园，<br />让每一株植物都有专属守护者。
          </p>
        </div>
        <div class="text-sm opacity-80">
          © {{ new Date().getFullYear() }} School Plant System
        </div>
      </div>

      <!-- Right Side -->
      <div class="w-full md:w-1/2 p-8 md:p-12 bg-white">
        <div class="text-center mb-8 md:hidden">
          <h2 class="text-2xl font-bold text-green-600">校园植物认养与养护</h2>
        </div>

        <a-tabs
          v-model:activeKey="activeTab"
          centered
          size="large"
          class="mb-6"
        >
          <a-tab-pane key="login" tab="账号登录" />
          <a-tab-pane key="register" tab="新用户注册" />
        </a-tabs>

        <a-form
          layout="vertical"
          :model="formState"
          @finish="onFinish"
          size="large"
        >
          <a-form-item
            name="username"
            :rules="[{ required: true, message: '请输入用户名!' }]"
          >
            <a-input v-model:value="formState.username" placeholder="用户名">
              <template #prefix
                ><UserOutlined class="text-gray-400"
              /></template>
            </a-input>
          </a-form-item>

          <template v-if="activeTab === 'register'">
            <a-form-item
              name="phone"
              :rules="[{ required: true, message: '请输入手机号!' }]"
            >
              <a-input v-model:value="formState.phone" placeholder="手机号">
                <template #prefix
                  ><PhoneOutlined class="text-gray-400"
                /></template>
              </a-input>
            </a-form-item>
            <a-form-item
              name="email"
              :rules="[
                { required: true, message: '请输入邮箱!' },
                { type: 'email', message: '邮箱格式不正确!' },
              ]"
            >
              <a-input v-model:value="formState.email" placeholder="邮箱">
                <template #prefix
                  ><MailOutlined class="text-gray-400"
                /></template>
              </a-input>
            </a-form-item>
          </template>

          <a-form-item
            name="password"
            :rules="[{ required: true, message: '请输入密码!' }]"
          >
            <a-input-password
              v-model:value="formState.password"
              placeholder="密码"
            >
              <template #prefix
                ><LockOutlined class="text-gray-400"
              /></template>
            </a-input-password>
          </a-form-item>

          <template v-if="activeTab === 'register'">
            <a-form-item
              name="confirmPassword"
              :rules="[
                { required: true, message: '请确认密码!' },
                { validator: validateConfirmPassword },
              ]"
            >
              <a-input-password
                v-model:value="formState.confirmPassword"
                placeholder="确认密码"
              >
                <template #prefix
                  ><SafetyOutlined class="text-gray-400"
                /></template>
              </a-input-password>
            </a-form-item>
            <a-form-item name="role" label="注册角色">
              <a-select v-model:value="formState.role">
                <a-select-option value="USER"
                  >普通用户 (学生/老师)</a-select-option
                >
                <a-select-option value="MAINTAINER">校园养护员</a-select-option>
              </a-select>
            </a-form-item>
          </template>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              class="w-full h-12 text-lg font-medium shadow-md hover:shadow-lg transition-all"
              :loading="loading"
            >
              {{ activeTab === "login" ? "立即登录" : "立即注册" }}
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive } from "vue";
import {
  UserOutlined,
  LockOutlined,
  MailOutlined,
  PhoneOutlined,
  SafetyOutlined,
} from "@ant-design/icons-vue";
import { message } from "ant-design-vue";
import { useRouter } from "vue-router";
import { login, register } from "@/api/auth";
import CryptoJS from "crypto-js";

const router = useRouter();
const activeTab = ref("login");
const loading = ref(false);

const formState = reactive({
  username: "",
  password: "",
  confirmPassword: "",
  phone: "",
  email: "",
  role: "USER",
});

const validateConfirmPassword = async (_rule: any, value: string) => {
  if (value && value !== formState.password) {
    return Promise.reject("两次输入的密码不一致!");
  }
  return Promise.resolve();
};

const onFinish = async (values: any) => {
  loading.value = true;
  try {
    const encryptedValues = { ...values };
    if (values.password) {
      encryptedValues.password = CryptoJS.MD5(values.password).toString();
    }
    if (values.confirmPassword) {
      encryptedValues.confirmPassword = CryptoJS.MD5(
        values.confirmPassword,
      ).toString();
    }

    if (activeTab.value === "login") {
      const res: any = await login(encryptedValues);
      console.log("Login Response received in Component:", res);

      // Defensive check: Handle both wrapped and unwrapped scenarios just in case
      let userData = res;
      if (res.data && res.code === 200) {
        userData = res.data;
      }

      if (!userData.tokenInfo || !userData.tokenInfo.tokenValue) {
        console.error("Invalid token structure:", userData);
        message.error("登录失败：无效的令牌响应");
        return;
      }

      localStorage.setItem("token", userData.tokenInfo.tokenValue);
      localStorage.setItem("role", userData.roleType);
      localStorage.setItem("username", userData.username);

      message.success("登录成功");
      console.log("Token stored. Role:", userData.roleType);
      console.log("Navigating to /user/overview...");

      // Use replace to avoid back-button issues
      await router.replace("/user/overview");
      console.log("Navigation called.");
    } else {
      await register(encryptedValues);
      message.success("注册成功，请登录");
      activeTab.value = "login";
      formState.password = "";
      formState.confirmPassword = "";
    }
  } catch (error) {
    console.error("Login/Register Error:", error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
/* Add any additional custom styles here */
</style>
