<template>
  <div class="app-container">
    <a-card :bordered="false">
      <!-- Search Form -->
      <a-form layout="inline" :model="queryParams" @finish="handleQuery">
        <a-form-item label="用户ID">
          <a-input v-model:value="queryParams.userId" placeholder="申请人ID" allowClear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryParams.status" placeholder="审核状态" style="width: 150px" allowClear>
            <a-select-option value="PENDING">待审核</a-select-option>
            <a-select-option value="INITIAL_PASSED">初审通过</a-select-option>
            <a-select-option value="REVIEW_PASSED">复审通过</a-select-option>
            <a-select-option value="APPROVED">已通过</a-select-option>
            <a-select-option value="REJECTED">已驳回</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">查询</a-button>
          <a-button style="margin-left: 8px" @click="resetQuery">重置</a-button>
        </a-form-item>
      </a-form>

      <!-- Table -->
      <a-table
        style="margin-top: 24px"
        :columns="columns"
        :data-source="list"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a @click="handleAudit(record)" v-if="canAudit(record.status)">审核</a>
              <a @click="handleViewLogs(record)">记录</a>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- Audit Modal -->
      <a-modal
        v-model:visible="auditVisible"
        title="审核认养申请"
        @ok="submitAudit"
        :confirmLoading="auditLoading"
      >
        <a-descriptions bordered :column="1" size="small">
          <a-descriptions-item label="申请ID">{{ currentApp.id }}</a-descriptions-item>
          <a-descriptions-item label="申请人ID">{{ currentApp.userId }}</a-descriptions-item>
          <a-descriptions-item label="植物ID">{{ currentApp.plantId }}</a-descriptions-item>
          <a-descriptions-item label="养护经验">{{ currentApp.careExperience }}</a-descriptions-item>
          <a-descriptions-item label="当前状态">
             <a-tag :color="getStatusColor(currentApp.status)">{{ getStatusText(currentApp.status) }}</a-tag>
          </a-descriptions-item>
        </a-descriptions>
        
        <a-divider />
        
        <a-form layout="vertical">
          <a-form-item label="审核结果" required>
            <a-radio-group v-model:value="auditForm.action">
              <a-radio value="PASS">通过</a-radio>
              <a-radio value="REJECT">驳回</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item label="审核意见" :required="auditForm.action === 'REJECT'">
            <a-textarea v-model:value="auditForm.comment" placeholder="请输入审核意见" :rows="4" />
          </a-form-item>
        </a-form>
      </a-modal>

      <!-- Logs Drawer -->
      <a-drawer
        v-model:visible="logsVisible"
        title="审核记录"
        width="600"
        placement="right"
      >
        <a-timeline>
          <a-timeline-item v-for="log in auditLogs" :key="log.id" :color="log.auditAction === 'PASS' ? 'green' : 'red'">
            <p>
              <strong>{{ log.auditStage }} - {{ log.auditAction === 'PASS' ? '通过' : '驳回' }}</strong>
              <span style="float: right; color: #999">{{ log.createdAt }}</span>
            </p>
            <p>审核人: {{ log.auditorName }}</p>
            <p v-if="log.comment">意见: {{ log.comment }}</p>
          </a-timeline-item>
        </a-timeline>
      </a-drawer>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { listApplications, auditApplication, listAuditLogs } from '@/api/adoption';
import { message } from 'ant-design-vue';

const queryParams = reactive({
  userId: undefined,
  status: undefined,
});

const list = ref([]);
const loading = ref(false);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '申请人ID', dataIndex: 'userId', width: 100 },
  { title: '植物ID', dataIndex: 'plantId', width: 100 },
  { title: '养护经验', dataIndex: 'careExperience', ellipsis: true },
  { title: '状态', key: 'status', width: 120 },
  { title: '申请时间', dataIndex: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
];

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待审核',
    INITIAL_PASSED: '初审通过',
    REVIEW_PASSED: '复审通过',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    CANCELLED: '已取消'
  };
  return map[status] || status;
};

const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'orange',
    INITIAL_PASSED: 'blue',
    REVIEW_PASSED: 'cyan',
    APPROVED: 'green',
    REJECTED: 'red',
    CANCELLED: 'default'
  };
  return map[status] || 'default';
};

const canAudit = (status: string) => {
  return ['PENDING', 'INITIAL_PASSED', 'REVIEW_PASSED'].includes(status);
};

// Audit Logic
const auditVisible = ref(false);
const auditLoading = ref(false);
const currentApp = ref<any>({});
const auditForm = reactive({
  action: 'PASS',
  comment: '',
});

const handleAudit = (record: any) => {
  currentApp.value = record;
  auditForm.action = 'PASS';
  auditForm.comment = '';
  auditVisible.value = true;
};

const submitAudit = async () => {
  if (auditForm.action === 'REJECT' && !auditForm.comment) {
    message.warning('驳回时必须填写审核意见');
    return;
  }
  
  auditLoading.value = true;
  try {
    await auditApplication({
      id: currentApp.value.id,
      action: auditForm.action,
      comment: auditForm.comment
    });
    message.success('审核完成');
    auditVisible.value = false;
    fetchData();
  } finally {
    auditLoading.value = false;
  }
};

// Logs Logic
const logsVisible = ref(false);
const auditLogs = ref<any[]>([]);

const handleViewLogs = async (record: any) => {
  const res = await listAuditLogs(record.id);
  auditLogs.value = res.data;
  logsVisible.value = true;
};

// Query Logic
const handleQuery = () => {
  pagination.current = 1;
  fetchData();
};

const resetQuery = () => {
  queryParams.userId = undefined;
  queryParams.status = undefined;
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
    const res = await listApplications(params);
    list.value = res.data.records;
    pagination.total = res.data.total;
  } finally {
    loading.value = false;
  }
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchData();
};

onMounted(() => {
  fetchData();
});
</script>
