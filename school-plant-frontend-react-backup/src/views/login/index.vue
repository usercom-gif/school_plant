<template>
  <div class="login-container">
    <a-card
      class="login-card"
      :title="isRegister ? '新用户注册' : '校园植物认养与养护系统'"
      :bordered="false"
    >
      <a-form
        :model="formState"
        @finish="onFinish"
        layout="vertical"
        ref="formRef"
      >
        <!-- 注册特有字段 -->
        <template v-if="isRegister">
          <a-form-item
            label="用户名"
            name="username"
            :rules="[
              { required: true, message: '请输入用户名' },
              { min: 4, max: 20, message: '长度在4-20字符之间' },
            ]"
          >
            <a-input
              v-model:value="formState.username"
              placeholder="4-20位字母/数字"
            >
              <template #prefix><UserOutlined /></template>
            </a-input>
          </a-form-item>

          <a-form-item
            label="邮箱"
            name="email"
            :rules="[
              { required: true, message: '请输入邮箱' },
              { type: 'email', message: '格式不正确' },
            ]"
          >
            <a-input v-model:value="formState.email" placeholder="常用邮箱">
              <template #prefix><MailOutlined /></template>
            </a-input>
          </a-form-item>

          <a-form-item
            label="手机号"
            name="phone"
            :rules="[
              { required: true, message: '请输入手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '格式不正确' },
            ]"
          >
            <a-input v-model:value="formState.phone" placeholder="11位手机号">
              <template #prefix><PhoneOutlined /></template>
            </a-input>
          </a-form-item>
        </template>

        <!-- 登录/注册共有字段 -->
        <a-form-item
          v-if="!isRegister"
          label="用户名"
          name="username"
          :rules="[{ required: true, message: '请输入用户名' }]"
        >
          <a-input
            v-model:value="formState.username"
            placeholder="请输入用户名"
          >
            <template #prefix><UserOutlined /></template>
          </a-input>
        </a-form-item>

        <a-form-item
          label="密码"
          name="password"
          :rules="[
            { required: true, message: '请输入密码' },
            isRegister
              ? { min: 6, max: 20, message: '长度在6-20字符之间' }
              : {},
          ]"
        >
          <a-input-password
            v-model:value="formState.password"
            placeholder="请输入密码"
          >
            <template #prefix><LockOutlined /></template>
          </a-input-password>
        </a-form-item>

        <template v-if="isRegister">
          <a-form-item
            label="确认密码"
            name="confirmPassword"
            :rules="[
              { required: true, message: '请确认密码' },
              { validator: validateConfirmPassword },
            ]"
          >
            <a-input-password
              v-model:value="formState.confirmPassword"
              placeholder="再次输入密码"
            >
              <template #prefix><LockOutlined /></template>
            </a-input-password>
          </a-form-item>
        </template>

        <a-form-item>
          <a-button type="primary" html-type="submit" block :loading="loading">
            {{ isRegister ? "立即注册" : "登录" }}
          </a-button>
        </a-form-item>

        <div class="login-footer">
          <a @click="toggleMode">{{
            isRegister ? "已有账号？去登录" : "没有账号？立即注册"
          }}</a>
        </div>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import {
  UserOutlined,
  LockOutlined,
  MailOutlined,
  PhoneOutlined,
} from "@ant-design/icons-vue";
import { message } from "ant-design-vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/user";
import { register } from "@/api/user";
import md5 from "js-md5";

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const isRegister = ref(false);
const formRef = ref();

const formState = reactive({
  username: "",
  password: "",
  confirmPassword: "",
  email: "",
  phone: "",
});

const validateConfirmPassword = async (_rule: any, value: string) => {
  if (value !== formState.password) {
    return Promise.reject("两次输入的密码不一致");
  }
  return Promise.resolve();
};

const toggleMode = () => {
  isRegister.value = !isRegister.value;
  // Use nextTick to ensure form fields are rendered before resetting or fetching code
  // But resetFields() only works on existing fields.
  // Actually, resetting form when switching mode is tricky because fields change.
  // Better to just clear the reactive object manually or let the user re-type.
  // But let's try to just clear the form state values.
  formState.username = "";
  formState.password = "";
  formState.confirmPassword = "";
  formState.email = "";
  formState.phone = "";
};

onMounted(() => {
  const msg = sessionStorage.getItem("loginMessage");
  if (msg) {
    message.success(msg);
    sessionStorage.removeItem("loginMessage");
  }
});

const onFinish = async (values: any) => {
  loading.value = true;
  try {
    if (isRegister.value) {
      // 注册逻辑
      const encryptedPwd = (md5 as any)(values.password);
      // confirmPassword 校验已经在前端完成，后端校验时也需要一致
      // 虽然 RegisterRequest 中有 confirmPassword，但我们这里直接传加密后的
      await register({
        ...values,
        password: encryptedPwd,
        confirmPassword: encryptedPwd,
      });
      message.success("注册成功，请登录");
      isRegister.value = false;
      formRef.value?.resetFields();
    } else {
      // 登录逻辑
      const encryptedPwd = (md5 as any)(values.password);
      await userStore.login({
        username: values.username,
        password: encryptedPwd,
      });
      message.success("登录成功");
      router.push("/");
    }
  } catch (error: any) {
    message.error(
      error.message || (isRegister.value ? "注册失败" : "登录失败"),
    );
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f0f2f5;
  background-image: url("https://gw.alipayobjects.com/zos/rmsportal/TVYTbAXWheQpRcWDaDMu.svg");
}

.login-card {
  width: 400px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.login-footer {
  text-align: center;
  margin-top: 16px;
}

.captcha-img {
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}
</style>
