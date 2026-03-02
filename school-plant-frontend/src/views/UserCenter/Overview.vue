<template>
  <div class="p-4 space-y-6">
    <!-- Welcome Header -->
    <div
      class="bg-gradient-to-r from-green-500 to-emerald-600 rounded-xl p-8 text-white shadow-lg relative overflow-hidden"
    >
      <div class="relative z-10">
        <h1 class="text-3xl font-bold mb-2">欢迎回来, {{ username }} 👋</h1>
        <p class="text-green-100 text-lg opacity-90">
          今天是 {{ currentDate }}，校园里的植物正等着你的呵护。
        </p>
      </div>
      <!-- Decorative background elements -->
      <div
        class="absolute right-0 bottom-0 opacity-10 transform translate-x-1/4 translate-y-1/4"
      >
        <svg
          width="300"
          height="300"
          viewBox="0 0 24 24"
          fill="currentColor"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z"
            fill="currentColor"
          />
        </svg>
      </div>
    </div>

    <!-- Statistics Cards -->
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card hoverable class="h-full border-l-4 border-green-500">
          <a-statistic
            title="校园植物总数"
            :value="stats.totalPlants"
            :value-style="{ color: '#3f8600' }"
          >
            <template #prefix>
              <environment-outlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card hoverable class="h-full border-l-4 border-blue-500">
          <a-statistic
            title="待完成任务"
            :value="stats.pendingTasks"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <schedule-outlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card hoverable class="h-full border-l-4 border-orange-500">
          <a-statistic
            title="我的申请"
            :value="stats.myApplications"
            :value-style="{ color: '#fa8c16' }"
          >
            <template #prefix>
              <file-search-outlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card hoverable class="h-full border-l-4 border-red-500">
          <a-statistic
            title="已处理异常"
            :value="stats.resolvedAbnormalities"
            :value-style="{ color: '#cf1322' }"
          >
            <template #prefix>
              <safety-certificate-outlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[24, 24]">
      <!-- Quick Actions -->
      <a-col :xs="24" :lg="16">
        <a-card title="快捷操作" :bordered="false" class="shadow-sm h-full">
          <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
            <div
              v-for="action in quickActions"
              :key="action.title"
              class="flex flex-col items-center justify-center p-6 bg-gray-50 rounded-xl cursor-pointer hover:bg-green-50 hover:text-green-600 transition-all group"
              @click="router.push(action.path)"
            >
              <component
                :is="action.icon"
                class="text-3xl mb-3 text-gray-400 group-hover:text-green-500 transition-colors"
              />
              <span class="font-medium">{{ action.title }}</span>
            </div>
          </div>
        </a-card>
      </a-col>

      <!-- System Notice / Tips -->
      <a-col :xs="24" :lg="8">
        <a-card title="养护小贴士" :bordered="false" class="shadow-sm h-full">
          <a-list item-layout="horizontal" :data-source="tips">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta :description="item.content">
                  <template #title>
                    <span class="text-green-600 font-medium">
                      <bulb-outlined class="mr-1" /> {{ item.title }}
                    </span>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import dayjs from "dayjs";
import {
  EnvironmentOutlined,
  ScheduleOutlined,
  FileSearchOutlined,
  SafetyCertificateOutlined,
  SearchOutlined,
  CameraOutlined,
  CarryOutOutlined,
  UserOutlined,
  BulbOutlined,
} from "@ant-design/icons-vue";
import { getPlantList } from "@/api/plant";
import { getMyTasks } from "@/api/task";
import { getMyApplications } from "@/api/adoption";
import { getAbnormalityList } from "@/api/abnormality";

const router = useRouter();
const username = ref(localStorage.getItem("username") || "User");
const currentDate = computed(() => dayjs().format("YYYY年MM月DD日 dddd"));

const stats = ref({
  totalPlants: 0,
  pendingTasks: 0,
  myApplications: 0,
  resolvedAbnormalities: 0,
});

const quickActions = [
  { title: "浏览植物", icon: SearchOutlined, path: "/user/plant-search" },
  { title: "我的任务", icon: CarryOutOutlined, path: "/user/my-tasks" },
  { title: "异常上报", icon: CameraOutlined, path: "/user/report-abnormality" },
  { title: "个人中心", icon: UserOutlined, path: "/user/profile" },
];

const tips = [
  {
    title: "浇水适量",
    content: "不同植物对水分需求不同，浇水前请先确认土壤干湿度。",
  },
  {
    title: "定期修剪",
    content: "及时修剪枯黄枝叶，有助于植物集中养分生长。",
  },
  {
    title: "注意光照",
    content: "喜阴植物避免暴晒，喜阳植物需保证充足光照。",
  },
];

const fetchStats = async () => {
  try {
    // Fetch Total Plants
    const plantRes: any = await getPlantList({ page: 1, size: 1 });
    if (plantRes && plantRes.total !== undefined) {
      stats.value.totalPlants = Number(plantRes.total);
    }

    // Fetch My Tasks
    const taskRes: any = await getMyTasks({ page: 1, size: 1, status: "PENDING" });
    if (taskRes && taskRes.total !== undefined) {
      stats.value.pendingTasks = Number(taskRes.total);
    }

    // Fetch My Applications
    const appRes: any = await getMyApplications({ page: 1, size: 1 });
    if (appRes && appRes.total !== undefined) {
      stats.value.myApplications = Number(appRes.total);
    }

    // Fetch Resolved Abnormalities (Mocking 'resolved' query as API might not support direct count easily without list)
    // Actually getAbnormalityList supports status
    const abRes: any = await getAbnormalityList({
      page: 1,
      size: 1,
      status: "RESOLVED",
    });
    if (abRes && abRes.total !== undefined) {
      stats.value.resolvedAbnormalities = Number(abRes.total);
    }
  } catch (error) {
    console.error("Failed to fetch dashboard stats", error);
  }
};

onMounted(() => {
  fetchStats();
});
</script>

<style scoped>
/* Add any custom styles here if tailwind is not enough */
</style>
