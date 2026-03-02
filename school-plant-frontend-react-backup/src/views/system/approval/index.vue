<template>
  <div class="app-container" style="padding: 20px">
    <a-card :bordered="false">
      <!-- Search -->
      <a-form layout="inline" style="margin-bottom: 20px">
        <a-form-item label="状态">
          <a-select v-model:value="queryParams.status" placeholder="请选择" style="width: 150px" allowClear>
            <a-select-option value="PENDING">待审批</a-select-option>
            <a-select-option value="APPROVED">已通过</a-select-option>
            <a-select-option value="REJECTED">已驳回</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleQuery">查询</a-button>
          <a-button style="margin-left: 8px" @click="resetQuery">重置</a-button>
        </a-form-item>
      </a-form>

      <!-- Table -->
      <a-table :columns="columns" :data-source="list" :loading="loading" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <span v-if="record.status === 'PENDING'">
              <a @click="handleApprove(record)">通过</a>
              <a-divider type="vertical" />
              <a @click="handleReject(record)" style="color: #ff4d4f">驳回</a>
            </span>
            <span v-else>-</span>
          </template>
          <template v-if="column.key === 'createdAt'">
            {{ formatDate(record.createdAt) }}
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Reject Modal -->
    <a-modal v-model:visible="rejectVisible" title="驳回申请" @ok="confirmReject">
      <a-form :label-col="{ span: 4 }">
        <a-form-item label="驳回原因">
          <a-textarea v-model:value="rejectComment" placeholder="请输入驳回原因" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { listApprovals, approveNameChange } from '@/api/user';
import dayjs from 'dayjs';

const loading = ref(false);
const list = ref([]);
const queryParams = ref<any>({
  status: 'PENDING'
});

const columns = [
  { title: '申请ID', dataIndex: 'id', key: 'id' },
  { title: '申请人ID', dataIndex: 'userId', key: 'userId' },
  { title: '原姓名', dataIndex: 'originalValue', key: 'originalValue' },
  { title: '新姓名', dataIndex: 'newValue', key: 'newValue' },
  { title: '申请理由', dataIndex: 'reason', key: 'reason' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '申请时间', dataIndex: 'createdAt', key: 'createdAt' },
  { title: '审批意见', dataIndex: 'comment', key: 'comment' },
  { title: '操作', key: 'action', width: 150 }
];

const getStatusColor = (status: string) => {
  const map: any = { PENDING: 'orange', APPROVED: 'green', REJECTED: 'red' };
  return map[status] || 'default';
};

const getStatusText = (status: string) => {
  const map: any = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' };
  return map[status] || status;
};

const formatDate = (date: string) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-';
};

const handleQuery = () => {
  loading.value = true;
  listApprovals(queryParams.value.status).then(res => {
    list.value = res.data;
  }).finally(() => {
    loading.value = false;
  });
};

const resetQuery = () => {
  queryParams.value.status = undefined;
  handleQuery();
};

const handleApprove = (record: any) => {
  Modal.confirm({
    title: '确认通过?',
    content: `确认将用户姓名修改为 "${record.newValue}" 吗?`,
    onOk: () => {
      approveNameChange(record.id, 'APPROVED', '同意').then(() => {
        message.success('已通过');
        handleQuery();
      });
    }
  });
};

const rejectVisible = ref(false);
const rejectComment = ref('');
const currentRecord = ref<any>(null);

const handleReject = (record: any) => {
  currentRecord.value = record;
  rejectComment.value = '';
  rejectVisible.value = true;
};

const confirmReject = () => {
  if (!rejectComment.value) {
    message.error('请输入驳回原因');
    return;
  }
  approveNameChange(currentRecord.value.id, 'REJECTED', rejectComment.value).then(() => {
    message.success('已驳回');
    rejectVisible.value = false;
    handleQuery();
  });
};

onMounted(() => {
  handleQuery();
});
</script>
