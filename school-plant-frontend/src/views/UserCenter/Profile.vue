<template>
  <div class="max-w-4xl mx-auto p-4">
    <a-card :bordered="false" class="shadow-sm rounded-lg">
      <a-tabs v-model:activeKey="activeTab">
        <!-- Tab 1: 个人信息卡片 -->
        <a-tab-pane key="1" tab="个人信息">
          <div class="py-4">
            <UserProfileCard />
          </div>
        </a-tab-pane>

        <!-- Tab 2: 编辑资料 -->
        <a-tab-pane key="2" tab="编辑资料">
          <div class="max-w-xl mx-auto py-6">
            <a-form
              :model="profileForm"
              layout="vertical"
              @finish="handleUpdateProfile"
            >
              <a-form-item label="头像">
                <div class="flex items-center gap-4">
                  <a-avatar :size="64" :src="profileForm.avatarUrl">
                    <template #icon><UserOutlined /></template>
                  </a-avatar>
                  <a-upload
                    name="file"
                    action="/api/common/upload"
                    :show-upload-list="false"
                    :headers="uploadHeaders"
                    @change="handleAvatarChange"
                  >
                    <a-button type="dashed">
                      <UploadOutlined /> 更换头像
                    </a-button>
                  </a-upload>
                </div>
              </a-form-item>

              <a-form-item label="真实姓名" name="realName" :rules="[{ required: true, message: '请输入真实姓名' }]">
                <a-input v-model:value="profileForm.realName" placeholder="请输入真实姓名">
                  <template #prefix><UserOutlined class="text-gray-400" /></template>
                </a-input>
              </a-form-item>

              <a-form-item label="手机号码" name="phone" :rules="[{ required: true, message: '请输入手机号码' }]">
                <a-input v-model:value="profileForm.phone" placeholder="请输入手机号码">
                  <template #prefix><PhoneOutlined class="text-gray-400" /></template>
                </a-input>
              </a-form-item>

              <a-form-item label="电子邮箱" name="email" :rules="[{ type: 'email', message: '请输入有效的邮箱地址' }]">
                <a-input v-model:value="profileForm.email" placeholder="请输入电子邮箱">
                  <template #prefix><MailOutlined class="text-gray-400" /></template>
                </a-input>
              </a-form-item>

              <a-form-item class="mt-8">
                <a-button type="primary" html-type="submit" :loading="updatingProfile" block size="large">
                  保存修改
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </a-tab-pane>

        <!-- Tab 3: 修改密码 -->
        <a-tab-pane key="3" tab="安全设置">
          <div class="max-w-xl mx-auto py-6">
            <a-alert
              message="为了账号安全，建议定期修改密码。"
              type="info"
              show-icon
              class="mb-6"
            />
            
            <a-form
              ref="passwordFormRef"
              :model="passwordForm"
              layout="vertical"
              @finish="handleUpdatePassword"
            >
              <a-form-item 
                label="当前密码" 
                name="oldPassword" 
                :rules="[{ required: true, message: '请输入当前密码' }]"
              >
                <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入当前密码">
                  <template #prefix><LockOutlined class="text-gray-400" /></template>
                </a-input-password>
              </a-form-item>

              <a-form-item 
                label="新密码" 
                name="newPassword" 
                :rules="[
                  { required: true, message: '请输入新密码' },
                  { min: 6, message: '密码长度不能少于6位' }
                ]"
              >
                <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码">
                  <template #prefix><LockOutlined class="text-gray-400" /></template>
                </a-input-password>
              </a-form-item>

              <a-form-item 
                label="确认新密码" 
                name="confirmPassword" 
                :rules="[
                  { required: true, message: '请再次输入新密码' },
                  { validator: validateConfirmPassword }
                ]"
              >
                <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码">
                  <template #prefix><LockOutlined class="text-gray-400" /></template>
                </a-input-password>
              </a-form-item>

              <a-form-item class="mt-8">
                <a-button type="primary" html-type="submit" :loading="updatingPassword" block size="large">
                  修改密码
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import type { UploadChangeParam } from 'ant-design-vue';
import { UserOutlined, PhoneOutlined, MailOutlined, LockOutlined, UploadOutlined } from '@ant-design/icons-vue';
import { getUserProfile, updateUserProfile, updateUserPassword } from '@/api/user';
import type { UpdateProfileParams, UpdatePasswordParams } from '@/api/user';
import UserProfileCard from './ProfileCard.vue';

const activeTab = ref('1');
const updatingProfile = ref(false);
const updatingPassword = ref(false);
const passwordFormRef = ref();

// Headers for upload (add token if needed, but usually handled by cookies or interceptor if using axios)
// However, a-upload uses its own XHR. Need to add Authorization header manually if using JWT in header.
// Assuming token is in localStorage 'token' or similar.
const token = localStorage.getItem('token');
const uploadHeaders = token ? { satoken: token } : {}; 
// Wait, Sa-Token default header name is usually 'satoken' or 'Authorization'.
// Let's check request.ts or login logic to see token key.
// Usually 'satoken' based on backend config: sa-token.token-name: satoken

const profileForm = reactive<UpdateProfileParams>({
  realName: '',
  phone: '',
  email: '',
  avatarUrl: '',
});

const passwordForm = reactive<UpdatePasswordParams>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

// Fetch user data for initial form values
const fetchUserData = async () => {
  try {
    const res = await getUserProfile();
    if (res) {
      profileForm.realName = res.name;
      profileForm.phone = res.phone;
      profileForm.email = res.email;
      profileForm.avatarUrl = res.avatarUrl;
    }
  } catch (error) {
    console.error('Failed to fetch user profile:', error);
  }
};

onMounted(() => {
  fetchUserData();
});

const handleAvatarChange = (info: UploadChangeParam) => {
  if (info.file.status === 'done') {
    // Get url from response
    // Response format: { code: 200, msg: "success", data: { url: "..." } }
    const response = info.file.response;
    if (response && response.code === 200) {
      profileForm.avatarUrl = response.data.url;
      message.success('头像上传成功');
    } else {
      message.error(response?.msg || '头像上传失败');
    }
  } else if (info.file.status === 'error') {
    message.error('头像上传失败');
  }
};

const handleUpdateProfile = async (values: UpdateProfileParams) => {
  updatingProfile.value = true;
  try {
    // Include avatarUrl from state, as it's not an input field in the form values directly (unless hidden input)
    // But values only contains form fields.
    // We should merge or use profileForm directly if bound with v-model.
    // ant-design-vue form finish returns values from form items.
    // Let's manually construct payload.
    const payload = {
      ...values,
      avatarUrl: profileForm.avatarUrl
    };
    
    await updateUserProfile(payload);
    message.success('个人资料更新成功');
    // Refresh data?
    fetchUserData();
  } catch (error: any) {
    message.error(error.message || '更新失败');
  } finally {
    updatingProfile.value = false;
  }
};

const validateConfirmPassword = async (_rule: any, value: string) => {
  if (value && value !== passwordForm.newPassword) {
    return Promise.reject('两次输入的密码不一致');
  }
  return Promise.resolve();
};

const handleUpdatePassword = async (values: UpdatePasswordParams) => {
  updatingPassword.value = true;
  try {
    await updateUserPassword(values);
    message.success('密码修改成功，请重新登录');
    // Logout logic or redirect
    // usually clear token and redirect to login
    localStorage.removeItem('token');
    // window.location.href = '/login'; 
    // Or router push
    // import router from '@/router';
    // router.push('/login');
  } catch (error: any) {
    message.error(error.message || '密码修改失败');
  } finally {
    updatingPassword.value = false;
  }
};
</script>

<style scoped>
/* Add any custom styles here */
</style>
