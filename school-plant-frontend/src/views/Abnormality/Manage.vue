<template>
  <div>
    <!-- Filter -->
    <a-card class="mb-4 shadow-sm" :bordered="false">
      <a-form layout="inline" :model="queryParams" @finish="fetchData">
        <a-form-item label="状态">
          <a-select v-model:value="queryParams.status" placeholder="全部状态" style="width: 120px" allow-clear>
            <a-select-option value="PENDING">待分派</a-select-option>
            <a-select-option value="ASSIGNED">处理中</a-select-option>
            <a-select-option value="RESOLVED">已解决</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading">查询</a-button>
          <a-button v-if="role === 'USER'" class="ml-2" type="primary" ghost @click="router.push('/user/report-abnormality')">上报异常</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- Table -->
    <a-card class="shadow-sm" :bordered="false">
      <div class="mb-4 text-lg font-bold">异常工单管理</div>
      
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a @click="viewDetail(record)">详情</a>
              
              <!-- Admin Actions -->
              <a v-if="role === 'ADMIN' && record.status === 'PENDING'" @click="openAssign(record)">分派</a>
              
              <!-- Maintainer Actions -->
              <a v-if="role === 'MAINTAINER' && record.status === 'ASSIGNED'" @click="openResolve(record)">处理</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Detail Modal -->
    <a-modal v-model:open="detailVisible" title="异常详情" :footer="null" width="700px">
      <a-descriptions bordered :column="1" v-if="currentRecord">
        <a-descriptions-item label="植物ID">{{ currentRecord.plantId }}</a-descriptions-item>
        <a-descriptions-item label="异常类型">{{ currentRecord.abnormalityType }}</a-descriptions-item>
        <a-descriptions-item label="描述">{{ currentRecord.description }}</a-descriptions-item>
        <a-descriptions-item label="AI建议">
          <div class="bg-gray-50 p-2 rounded text-gray-600 text-sm whitespace-pre-wrap">{{ currentRecord.suggestedSolution }}</div>
        </a-descriptions-item>
        <a-descriptions-item label="现场图片">
          <div class="flex gap-2 flex-wrap">
            <a-image 
              v-for="(url, idx) in parseImages(currentRecord.imageUrls)" 
              :key="idx" 
              :src="url" 
              :width="100" 
            />
          </div>
        </a-descriptions-item>
        
        <template v-if="currentRecord.status === 'RESOLVED'">
           <a-descriptions-item label="处理结果">{{ currentRecord.resolutionDescription }}</a-descriptions-item>
           <a-descriptions-item label="使用材料">{{ currentRecord.materialsUsed }}</a-descriptions-item>
           <a-descriptions-item label="效果评估">{{ currentRecord.effectEvaluation }}</a-descriptions-item>
           <a-descriptions-item label="处理后图片">
            <div class="flex gap-2 flex-wrap">
              <a-image 
                v-for="(url, idx) in parseImages(currentRecord.resolutionImageUrls)" 
                :key="idx" 
                :src="url" 
                :width="100" 
              />
            </div>
          </a-descriptions-item>
        </template>
      </a-descriptions>
    </a-modal>

    <!-- Assign Modal (Admin) -->
    <a-modal v-model:open="assignVisible" title="分派工单" @ok="submitAssign" :confirmLoading="actionLoading">
      <a-form layout="vertical">
        <a-form-item label="选择养护员" required>
          <a-input-number v-model:value="assignMaintainerId" class="w-full" placeholder="输入养护员ID (暂无选择器)" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Resolve Modal (Maintainer) -->
    <a-modal v-model:open="resolveVisible" title="填写处理结果" @ok="submitResolve" :confirmLoading="actionLoading">
      <a-form layout="vertical">
        <a-form-item label="处理措施" required>
          <a-textarea v-model:value="resolveForm.resolution" :rows="3" />
        </a-form-item>
        <a-form-item label="使用材料" required>
          <a-input v-model:value="resolveForm.materials" />
        </a-form-item>
        <a-form-item label="效果评估" required>
          <a-input v-model:value="resolveForm.evaluation" />
        </a-form-item>
        <a-form-item label="处理后照片">
          <input type="file" accept="image/*" multiple @change="handleResolveFileChange" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router'; // Add router
import { getAbnormalityList, assignAbnormality, resolveAbnormality, type PlantAbnormality } from '@/api/abnormality';

const router = useRouter(); // Init router
const role = localStorage.getItem('role') || 'USER';
const userId = localStorage.getItem('userId');
const loading = ref(false);
const dataSource = ref<PlantAbnormality[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const queryParams = reactive({ status: undefined });

// Modals
const detailVisible = ref(false);
const assignVisible = ref(false);
const resolveVisible = ref(false);
const actionLoading = ref(false);
const currentRecord = ref<PlantAbnormality | null>(null);

// Assign Form
const assignMaintainerId = ref<number | undefined>(undefined);

// Resolve Form
const resolveForm = reactive({
  resolution: '',
  materials: '',
  evaluation: '',
  images: [] as File[],
});

// WebSocket
let socket: WebSocket | null = null;

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '类型', dataIndex: 'abnormalityType', width: 80 },
  { title: '描述', dataIndex: 'description', ellipsis: true },
  { title: 'AI建议', dataIndex: 'suggestedSolution', ellipsis: true },
  { title: '状态', key: 'status', width: 100 },
  { title: '上报时间', dataIndex: 'createdAt' },
  { title: '操作', key: 'action', width: 150 },
];

const getStatusColor = (status: string) => {
  const map: any = { PENDING: 'red', ASSIGNED: 'orange', RESOLVED: 'green' };
  return map[status] || 'default';
};

const getStatusLabel = (status: string) => {
  const map: any = { PENDING: '待分派', ASSIGNED: '处理中', RESOLVED: '已解决' };
  return map[status] || status;
};

const parseImages = (jsonStr?: string) => {
  if (!jsonStr) return [];
  try {
    return JSON.parse(jsonStr);
  } catch (e) {
    return [];
  }
};

const fetchData = async () => {
  loading.value = true;
  try {
    const res: any = await getAbnormalityList({
      page: pagination.current,
      size: pagination.pageSize,
      ...queryParams
    });
    dataSource.value = res.records;
    pagination.total = res.total;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchData();
};

const viewDetail = (record: PlantAbnormality) => {
  currentRecord.value = record;
  detailVisible.value = true;
};

const openAssign = (record: PlantAbnormality) => {
  currentRecord.value = record;
  assignMaintainerId.value = undefined;
  assignVisible.value = true;
};

const submitAssign = async () => {
  if (!currentRecord.value || !assignMaintainerId.value) return;
  actionLoading.value = true;
  try {
    await assignAbnormality(currentRecord.value.id, assignMaintainerId.value);
    message.success('分派成功');
    assignVisible.value = false;
    fetchData();
  } catch (e) {
    console.error(e);
  } finally {
    actionLoading.value = false;
  }
};

const openResolve = (record: PlantAbnormality) => {
  currentRecord.value = record;
  resolveForm.resolution = '';
  resolveForm.materials = '';
  resolveForm.evaluation = '';
  resolveForm.images = [];
  resolveVisible.value = true;
};

const handleResolveFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement;
  if (target.files) {
    resolveForm.images = Array.from(target.files);
  }
};

const submitResolve = async () => {
  if (!currentRecord.value) return;
  actionLoading.value = true;
  try {
    await resolveAbnormality({
      id: currentRecord.value.id,
      ...resolveForm
    });
    message.success('处理完成');
    resolveVisible.value = false;
    fetchData();
  } catch (e) {
    console.error(e);
  } finally {
    actionLoading.value = false;
  }
};

const initWebSocket = () => {
  if (!userId) return;
  // Use generic userId path, backend handles routing
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
  socket = new WebSocket(`${protocol}://localhost:8080/ws/abnormality/${userId}`);
  
  socket.onmessage = (event) => {
    message.info(event.data); // Show notification
    fetchData(); // Refresh list on update
  };
};

onMounted(() => {
  fetchData();
  initWebSocket();
});

onUnmounted(() => {
  if (socket) socket.close();
});
</script>
