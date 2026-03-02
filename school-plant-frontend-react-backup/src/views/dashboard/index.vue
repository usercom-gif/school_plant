<template>
  <div class="dashboard-container">
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1 class="welcome-title">欢迎回来，{{ userStore.name }}</h1>
        <p class="welcome-subtitle">今天是 {{ currentDate }}，祝您拥有美好的一天</p>
      </div>
      <div class="welcome-image">
        <img src="https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png" alt="welcome" />
      </div>
    </div>

    <div class="overview-section">
      <h2 class="section-title">数据概览</h2>
      <a-row :gutter="[24, 24]">
        <a-col :xs="24" :sm="12" :md="6" v-for="item in statItems" :key="item.title">
          <a-card :bordered="false" class="stat-card">
            <a-statistic :title="item.title" :value="item.value" :precision="0" :value-style="{ color: item.color }">
              <template #prefix>
                <component :is="item.icon" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <div class="quick-access">
      <h2 class="section-title">快速开始</h2>
      <a-row :gutter="[24, 24]">
        <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in quickAccessItems" :key="item.path">
          <a-card hoverable class="access-card" @click="router.push(item.path)">
            <div class="card-content">
              <div class="icon-wrapper" :style="{ backgroundColor: item.color }">
                <component :is="item.icon" />
              </div>
              <div class="text-info">
                <h3>{{ item.title }}</h3>
                <p>{{ item.desc }}</p>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { useUserStore } from '@/store/user';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import { getDashboardStats } from '@/api/dashboard';
import { 
  UserOutlined, SearchOutlined, FormOutlined, WarningOutlined,
  CheckSquareOutlined, TrophyOutlined, MedicineBoxOutlined,
  SettingOutlined, AppstoreOutlined, AuditOutlined, FileTextOutlined,
  ScheduleOutlined, CheckCircleOutlined, EnvironmentOutlined,
  HeartOutlined, ToolOutlined, ReadOutlined
} from '@ant-design/icons-vue';

const userStore = useUserStore();
const router = useRouter();
const currentDate = dayjs().format('YYYY年MM月DD日');

const statItems = ref<any[]>([]);

onMounted(async () => {
  try {
    const res = await getDashboardStats();
    statItems.value = res.data;
  } catch (e) {
    console.error('Failed to fetch dashboard stats', e);
    // Fallback to empty or static if needed, but requirements say "Real Data"
    // If fail, maybe show error or empty list
  }
});

const quickAccessItems = computed(() => {
  const role = userStore.roles[0];
  
  if (role === 'ADMIN') {
    return [
      { title: '用户管理', desc: '管理系统用户信息', path: '/system/user', icon: 'UserOutlined', color: '#1890ff' },
      { title: '植物管理', desc: '维护校园植物数据', path: '/plant/admin', icon: 'AppstoreOutlined', color: '#52c41a' },
      { title: '认养审核', desc: '审批学生认养申请', path: '/adoption/audit', icon: 'CheckSquareOutlined', color: '#faad14' },
      { title: '系统设置', desc: '配置系统参数', path: '/system/config', icon: 'SettingOutlined', color: '#722ed1' }
    ];
  } else if (role === 'MAINTAINER') {
    return [
      { title: '异常处理', desc: '查看待处理异常', path: '/abnormality/handle', icon: 'MedicineBoxOutlined', color: '#ff4d4f' },
      { title: '任务跟踪', desc: '跟踪养护任务进度', path: '/task/track', icon: 'CheckSquareOutlined', color: '#1890ff' },
      { title: '处理记录', desc: '查看历史处理记录', path: '/personal/record', icon: 'FormOutlined', color: '#52c41a' },
      { title: '个人中心', desc: '查看个人信息', path: '/profile', icon: 'UserOutlined', color: '#faad14' }
    ];
  } else {
    // USER
    return [
      { title: '植物查询', desc: '浏览校园植物', path: '/plant/query', icon: 'SearchOutlined', color: '#1890ff' },
      { title: '认养申请', desc: '申请认养心仪植物', path: '/adoption/apply', icon: 'FormOutlined', color: '#52c41a' },
      { title: '异常上报', desc: '发现植物异常情况', path: '/abnormality/report', icon: 'WarningOutlined', color: '#ff4d4f' },
      { title: '成果查询', desc: '查看我的认养成果', path: '/achievement/query', icon: 'TrophyOutlined', color: '#faad14' }
    ];
  }
});
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
}

.welcome-banner {
  background: #fff;
  padding: 32px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.welcome-content h1 {
  font-size: 28px;
  color: #1f1f1f;
  margin-bottom: 12px;
  font-weight: 600;
}

.welcome-content p {
  color: #8c8c8c;
  font-size: 16px;
}

.welcome-image img {
  height: 160px;
}

.section-title {
  font-size: 20px;
  font-weight: 500;
  color: #1f1f1f;
  margin-bottom: 24px;
}

.overview-section {
  margin-bottom: 32px;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.access-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.access-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  align-items: center;
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: #fff;
  font-size: 24px;
}

.text-info h3 {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
  color: #1f1f1f;
}

.text-info p {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0;
}
</style>
