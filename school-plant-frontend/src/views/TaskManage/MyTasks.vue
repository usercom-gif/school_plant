<template>
  <div>
    <a-card class="shadow-sm" :bordered="false">
      <div class="mb-4 text-lg font-bold">我的养护任务</div>

      <a-list :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 3, xl: 4 }" :data-source="dataSource">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-card :title="item.taskType" hoverable class="h-full flex flex-col">
              <template #extra>
                <a-tag :color="getStatusColor(item.status)">{{ getStatusLabel(item.status) }}</a-tag>
              </template>
              
              <div class="flex-1">
                <p class="text-gray-500 text-sm mb-2">植物: <span class="text-gray-800 font-medium">{{ item.plantName }}</span></p>
                <p class="text-gray-600 mb-4">{{ item.taskDescription }}</p>
                <div class="text-xs text-gray-400 flex items-center gap-1">
                  <ClockCircleOutlined /> 截止: {{ item.dueDate }}
                </div>
              </div>

              <div class="mt-4 pt-4 border-t border-gray-100 flex justify-end">
                <a-button 
                  v-if="item.status === 'PENDING'" 
                  type="primary" 
                  size="small" 
                  @click="handleComplete(item)"
                >
                  打卡完成
                </a-button>
                <span v-else class="text-xs text-gray-400">
                  完成于 {{ item.completedDate || '-' }}
                </span>
              </div>
            </a-card>
          </a-list-item>
        </template>
      </a-list>
      
      <div class="mt-4 flex justify-center" v-if="pagination.total > 0">
        <a-pagination
          v-model:current="pagination.current"
          v-model:pageSize="pagination.pageSize"
          :total="pagination.total"
          @change="fetchData"
        />
      </div>
    </a-card>

    <!-- Complete Modal -->
    <a-modal
      v-model:open="completeModalVisible"
      title="任务打卡"
      @ok="submitComplete"
      :confirmLoading="completeLoading"
    >
      <a-form layout="vertical">
        <a-form-item label="上传照片凭证" required>
          <!-- Simplified image input for now, ideally an uploader -->
          <a-input v-model:value="completeImageUrl" placeholder="输入图片URL (模拟上传)" />
          <div class="mt-2 text-xs text-gray-400">请上传任务完成后的现场照片</div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { ClockCircleOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { getMyTasks, completeTask, type CareTask } from '@/api/task';

const loading = ref(false);
const dataSource = ref<CareTask[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 12,
  total: 0,
});

// Modal
const completeModalVisible = ref(false);
const completeLoading = ref(false);
const currentTask = ref<CareTask | null>(null);
const completeImageUrl = ref('');

const getStatusColor = (status: string) => {
  const map: any = { PENDING: 'orange', COMPLETED: 'green', OVERDUE: 'red' };
  return map[status] || 'default';
};

const getStatusLabel = (status: string) => {
  const map: any = { PENDING: '待完成', COMPLETED: '已完成', OVERDUE: '已逾期' };
  return map[status] || status;
};

const fetchData = async () => {
  loading.value = true;
  try {
    const res: any = await getMyTasks({
      page: pagination.current,
      size: pagination.pageSize,
    });
    dataSource.value = res.records;
    pagination.total = res.total;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleComplete = (task: CareTask) => {
  currentTask.value = task;
  completeImageUrl.value = '';
  completeModalVisible.value = true;
};

const submitComplete = async () => {
  if (!currentTask.value) return;
  if (!completeImageUrl.value) {
    message.warning('请上传照片凭证');
    return;
  }

  completeLoading.value = true;
  try {
    await completeTask({
      id: currentTask.value.id,
      imageUrl: completeImageUrl.value,
    });
    message.success('打卡成功');
    completeModalVisible.value = false;
    fetchData();
  } catch (e) {
    console.error(e);
  } finally {
    completeLoading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>
