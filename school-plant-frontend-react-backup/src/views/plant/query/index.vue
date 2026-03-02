<template>
  <div class="app-container">
    <a-card :bordered="false">
      <!-- 筛选区域 -->
      <a-form layout="inline" :model="queryParams" @finish="handleQuery">
        <a-form-item label="关键词">
          <a-input v-model:value="queryParams.keyword" placeholder="品种/位置" allowClear />
        </a-form-item>
        <a-form-item label="区域">
          <a-select v-model:value="queryParams.region" placeholder="种植区域" style="width: 120px" allowClear>
            <a-select-option value="中央校区">中央校区</a-select-option>
            <a-select-option value="东校区">东校区</a-select-option>
            <a-select-option value="西校区">西校区</a-select-option>
            <a-select-option value="南校区">南校区</a-select-option>
            <a-select-option value="北校区">北校区</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="养护难度">
          <a-select v-model:value="queryParams.careDifficulty" placeholder="难度" style="width: 100px" allowClear>
            <a-select-option :value="1">简单</a-select-option>
            <a-select-option :value="2">中等</a-select-option>
            <a-select-option :value="3">较难</a-select-option>
            <a-select-option :value="4">困难</a-select-option>
            <a-select-option :value="5">专家</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryParams.status" placeholder="认养状态" style="width: 100px" allowClear>
            <a-select-option value="AVAILABLE">待认养</a-select-option>
            <a-select-option value="ADOPTED">已认养</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">查询</a-button>
          <a-button style="margin-left: 8px" @click="resetQuery">重置</a-button>
        </a-form-item>
      </a-form>

      <!-- 植物列表 (卡片视图) -->
      <a-list
        :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 4, xxl: 6 }"
        :data-source="plantList"
        :loading="loading"
        :pagination="pagination"
        style="margin-top: 24px"
      >
        <template #renderItem="{ item }">
          <a-list-item>
            <a-card hoverable class="plant-card">
              <template #cover>
                <img 
                  :alt="item.name" 
                  :src="getImageUrl(item.imageUrls)" 
                  style="height: 200px; object-fit: cover;"
                />
              </template>
              <template #actions>
                <a-tooltip title="查看详情">
                  <span @click="showDetail(item)"><EyeOutlined /> 详情</span>
                </a-tooltip>
                <a-tooltip title="申请认养" v-if="canAdopt(item)">
                  <span @click="handleApply(item)" style="color: #52c41a;"><FormOutlined /> 认养</span>
                </a-tooltip>
              </template>
              <a-card-meta :title="item.name">
                <template #description>
                  <div class="plant-info">
                    <p>品种: {{ item.species }}</p>
                    <p>位置: {{ item.region }}</p>
                    <p>难度: <a-rate :value="item.careDifficulty" disabled count="5" style="font-size: 12px;" /></p>
                    <p>状态: 
                      <a-tag :color="item.status === 'AVAILABLE' ? 'green' : 'orange'">
                        {{ item.status === 'AVAILABLE' ? '待认养' : '已认养' }}
                      </a-tag>
                    </p>
                  </div>
                </template>
              </a-card-meta>
            </a-card>
          </a-list-item>
        </template>
      </a-list>

      <!-- 详情抽屉 -->
      <a-drawer
        v-model:visible="detailVisible"
        title="植物详情"
        width="640"
        placement="right"
      >
        <template v-if="currentPlant">
          <div style="text-align: center; margin-bottom: 20px;">
            <a-image
              :src="getImageUrl(currentPlant.imageUrls)"
              :width="300"
              style="border-radius: 8px;"
            />
          </div>
          <a-descriptions bordered :column="1">
            <a-descriptions-item label="植物名称">{{ currentPlant.name }}</a-descriptions-item>
            <a-descriptions-item label="植物编号">{{ currentPlant.plantCode }}</a-descriptions-item>
            <a-descriptions-item label="品种">{{ currentPlant.species }}</a-descriptions-item>
            <a-descriptions-item label="科属">{{ currentPlant.family }}</a-descriptions-item>
            <a-descriptions-item label="种植区域">{{ currentPlant.region }}</a-descriptions-item>
            <a-descriptions-item label="详细位置">{{ currentPlant.locationDescription }}</a-descriptions-item>
            <a-descriptions-item label="养护难度">
              <a-rate :value="currentPlant.careDifficulty" disabled />
            </a-descriptions-item>
            <a-descriptions-item label="种植年份">{{ currentPlant.plantingYear }}</a-descriptions-item>
            <a-descriptions-item label="光照需求">{{ currentPlant.lightRequirement }}</a-descriptions-item>
            <a-descriptions-item label="水分需求">{{ currentPlant.waterRequirement }}</a-descriptions-item>
            <a-descriptions-item label="植物描述">{{ currentPlant.description }}</a-descriptions-item>
            <a-descriptions-item label="养护要点">{{ currentPlant.careTips }}</a-descriptions-item>
          </a-descriptions>
          
          <div style="margin-top: 20px; text-align: center;" v-if="canAdopt(currentPlant)">
            <a-button type="primary" size="large" @click="handleApply(currentPlant)">立即申请认养</a-button>
          </div>
        </template>
      </a-drawer>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { listPlants } from '@/api/plant';
import { checkStatus } from '@/api/adoption';
import { useUserStore } from '@/store/user';
import { EyeOutlined, FormOutlined } from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';

const router = useRouter();
const userStore = useUserStore();

// 查询参数
const queryParams = reactive({
  keyword: undefined,
  region: undefined,
  careDifficulty: undefined,
  status: 'AVAILABLE', // 默认仅展示待认养
});

// 列表数据
const loading = ref(false);
const plantList = ref([]);
const pagination = reactive({
  current: 1,
  pageSize: 12, // 4 cols * 3 rows
  total: 0,
  onChange: (page: number, pageSize: number) => {
    pagination.current = page;
    pagination.pageSize = pageSize;
    fetchData();
  }
});

// 详情控制
const detailVisible = ref(false);
const currentPlant = ref<any>(null);

// 查询方法
const handleQuery = () => {
  pagination.current = 1;
  fetchData();
};

const resetQuery = () => {
  queryParams.keyword = undefined;
  queryParams.region = undefined;
  queryParams.careDifficulty = undefined;
  queryParams.status = 'AVAILABLE';
  handleQuery();
};

const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      ...queryParams,
      page: pagination.current,
      size: pagination.pageSize,
    };
    const res = await listPlants(params);
    plantList.value = res.data.records;
    pagination.total = res.data.total;
  } finally {
    loading.value = false;
  }
};

const getImageUrl = (jsonStr: string) => {
  try {
    if (!jsonStr) return 'https://via.placeholder.com/200x200?text=No+Image';
    const arr = JSON.parse(jsonStr);
    if (arr && arr.length > 0) {
      // Assuming URL is relative or absolute. If relative, prepend API base if needed, or rely on proxy.
      // Here assuming proxy handles /profile or similar.
      return arr[0]; 
    }
  } catch (e) {
    // maybe plain string?
    if (jsonStr && jsonStr.startsWith('http')) return jsonStr;
  }
  return 'https://via.placeholder.com/200x200?text=Plant';
};

const showDetail = (plant: any) => {
  currentPlant.value = plant;
  detailVisible.value = true;
};

// 认养相关
const canAdopt = (plant: any) => {
  // 仅普通用户且植物状态为AVAILABLE时显示
  return userStore.roles.includes('USER') && plant.status === 'AVAILABLE';
};

const handleApply = async (plant: any) => {
  try {
    // 1. 校验用户状态
    const res = await checkStatus();
    if (!res.data.canAdopt) {
      Modal.warning({
        title: '无法申请',
        content: res.data.message || '您已认养植物或有正在审核的申请，无法再次申请。',
      });
      return;
    }
    
    // 2. 跳转申请页
    router.push({
      path: '/adoption/apply',
      query: {
        plantId: plant.id,
        plantName: plant.name,
        species: plant.species
      }
    });
  } catch (e) {
    message.error('校验状态失败，请稍后重试');
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.plant-card {
  border-radius: 8px;
}
.plant-info p {
  margin-bottom: 4px;
  font-size: 13px;
  color: #666;
}
</style>
