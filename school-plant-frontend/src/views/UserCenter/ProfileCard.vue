<template>
  <a-card class="shadow-md rounded-lg overflow-hidden border-0">
    <a-skeleton :loading="loading" avatar active :paragraph="{ rows: 4 }">
      <div v-if="userInfo" class="flex flex-col items-center">
        <!-- Avatar -->
        <div class="mb-6 relative">
          <div class="w-24 h-24 rounded-full bg-green-100 flex items-center justify-center border-4 border-white shadow-sm overflow-hidden">
            <img v-if="userInfo.avatarUrl" :src="userInfo.avatarUrl" alt="Avatar" class="w-full h-full object-cover" />
            <span v-else class="text-4xl">🌿</span>
          </div>
          <a-tag :color="getRoleColor(userInfo.role)" class="absolute -bottom-2 left-1/2 transform -translate-x-1/2 shadow-sm">
            {{ userInfo.role }}
          </a-tag>
        </div>

        <!-- Info List -->
        <div class="w-full max-w-md bg-gray-50 rounded-lg p-6 space-y-4">
          <div class="flex justify-between items-center text-base">
            <span class="text-gray-500 flex items-center gap-2"><UserOutlined /> 账号</span>
            <span class="font-medium text-gray-800">{{ userInfo.account }}</span>
          </div>
          <div class="flex justify-between items-center text-base">
            <span class="text-gray-500 flex items-center gap-2"><IdcardOutlined /> 姓名</span>
            <span class="font-medium text-gray-800">{{ userInfo.name }}</span>
          </div>
          <div class="flex justify-between items-center text-base">
            <span class="text-gray-500 flex items-center gap-2"><NumberOutlined /> {{ userInfo.role === '普通用户' ? '学号/工号' : '工号' }}</span>
            <span class="font-medium text-gray-800">{{ userInfo.idNumber }}</span>
          </div>
          <div class="flex justify-between items-center text-base">
            <span class="text-gray-500 flex items-center gap-2"><PhoneOutlined /> 联系方式</span>
            <span class="font-medium text-gray-800">{{ userInfo.phone }}</span>
          </div>
          <div class="flex justify-between items-center text-base">
            <span class="text-gray-500 flex items-center gap-2"><MailOutlined /> 邮箱</span>
            <span class="font-medium text-gray-800">{{ userInfo.email || '未绑定' }}</span>
          </div>
          <div class="flex justify-between items-center text-base">
            <span class="text-gray-500 flex items-center gap-2"><CalendarOutlined /> 注册时间</span>
            <span class="font-medium text-gray-800">{{ userInfo.registerTime }}</span>
          </div>

          <div class="pt-4 mt-4 border-t border-gray-200">
            <div class="flex justify-between items-center">
              <span class="text-gray-500 flex items-center gap-2">
                <SafetyCertificateOutlined /> {{ getStatLabel(userInfo.role) }}
              </span>
              <span class="text-xl font-bold text-green-600">{{ userInfo.statisticNum }}</span>
            </div>
          </div>
        </div>
      </div>
      <a-alert v-else-if="error" :message="error" type="error" show-icon>
        <template #action>
          <a @click="fetchProfile">重试</a>
        </template>
      </a-alert>
    </a-skeleton>
  </a-card>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { UserOutlined, PhoneOutlined, SafetyCertificateOutlined, CalendarOutlined, IdcardOutlined, NumberOutlined } from '@ant-design/icons-vue';
import { getUserProfile, type UserProfile } from '@/api/user';

const loading = ref(true);
const error = ref<string | null>(null);
const userInfo = ref<UserProfile | null>(null);

const fetchProfile = async () => {
  loading.value = true;
  error.value = null;
  try {
    const res: any = await getUserProfile();
    userInfo.value = res;
  } catch (err) {
    error.value = '获取信息失败，请刷新重试';
  } finally {
    loading.value = false;
  }
};

const getRoleColor = (role: string) => {
  if (role === '管理员') return 'red';
  if (role === '养护员') return 'blue';
  return 'green';
};

const getStatLabel = (role: string) => {
  if (role === '管理员') return '负责审核申请数';
  if (role === '养护员') return '处理异常数';
  return '认养植物数量';
};

onMounted(() => {
  fetchProfile();
});
</script>
