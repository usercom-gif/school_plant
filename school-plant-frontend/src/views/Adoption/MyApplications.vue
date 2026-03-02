<template>
  <div>
    <a-card class="shadow-sm" :bordered="false">
      <div class="mb-4 text-lg font-bold">我的认养申请记录</div>

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
            <a @click="viewDetails(record)">详情</a>
            <a-divider type="vertical" />
            <a @click="viewLogs(record)">日志</a>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Details Modal -->
    <a-modal v-model:open="detailsModalVisible" title="申请详情" :footer="null">
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="申请植物">{{
          currentDetailRecord?.plantName
        }}</a-descriptions-item>
        <a-descriptions-item label="认养周期"
          >{{
            currentDetailRecord?.adoptionPeriodMonths
          }}
          个月</a-descriptions-item
        >
        <a-descriptions-item label="申请时间">{{
          currentDetailRecord?.createdAt
        }}</a-descriptions-item>
        <a-descriptions-item label="当前状态">
          <a-tag :color="getStatusColor(currentDetailRecord?.status || '')">
            {{ getStatusLabel(currentDetailRecord?.status || "") }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="申请理由">
          <div class="whitespace-pre-wrap">
            {{ currentDetailRecord?.careExperience }}
          </div>
        </a-descriptions-item>
        <a-descriptions-item
          label="驳回原因"
          v-if="currentDetailRecord?.status === 'REJECTED'"
        >
          <div class="text-red-500">
            {{ currentDetailRecord?.rejectionReason }}
          </div>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- Logs Modal -->
    <a-modal v-model:open="logsVisible" title="审核日志" :footer="null">
      <a-timeline>
        <a-timeline-item
          v-for="log in logs"
          :key="log.id"
          :color="log.action === 'REJECT' ? 'red' : 'green'"
        >
          <p class="mb-1 font-bold">
            {{
              log.action === "PASS" || log.action === "APPROVE"
                ? "审核通过"
                : "审核驳回"
            }}
          </p>
          <p class="mb-1 text-gray-500">{{ log.createdAt }}</p>
          <p v-if="log.comment">{{ log.comment }}</p>
        </a-timeline-item>
        <a-timeline-item v-if="logs.length === 0" color="gray"
          >暂无审核记录</a-timeline-item
        >
      </a-timeline>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue";
import {
  getMyApplications,
  getAuditLogs,
  type AdoptionApplication,
  type AdoptionAuditLog,
} from "@/api/adoption";

const loading = ref(false);
const dataSource = ref<AdoptionApplication[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const logsVisible = ref(false);
const logs = ref<AdoptionAuditLog[]>([]);

// --- Details Modal State ---
const detailsModalVisible = ref(false);
const currentDetailRecord = ref<AdoptionApplication | null>(null);

const columns = [
  { title: "ID", dataIndex: "id", width: 60 },
  { title: "植物名称", dataIndex: "plantName" },
  { title: "申请理由", dataIndex: "careExperience", ellipsis: true },
  { title: "驳回原因", dataIndex: "rejectionReason", ellipsis: true },
  { title: "状态", key: "status", width: 100 },
  { title: "申请时间", dataIndex: "createdAt" },
  { title: "操作", key: "action", width: 150 },
];

const getStatusColor = (status: string) => {
  const map: any = {
    PENDING: "orange",
    PASS: "green",
    ADOPTED: "green",
    REJECTED: "red",
    INITIAL_PASS: "cyan", // Initial pass color
  };
  return map[status] || "default";
};

const getStatusLabel = (status: string) => {
  const map: any = {
    PENDING: "待审核",
    PASS: "已通过",
    ADOPTED: "已认养",
    REJECTED: "已驳回",
    INITIAL_PASS: "初审通过",
  };
  return map[status] || status;
};

const fetchData = async () => {
  loading.value = true;
  try {
    const res: any = await getMyApplications({
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

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchData();
};

const viewDetails = (record: AdoptionApplication) => {
  currentDetailRecord.value = record;
  detailsModalVisible.value = true;
};

const viewLogs = async (record: AdoptionApplication) => {
  try {
    const res = await getAuditLogs(record.id);
    logs.value = res;
    logsVisible.value = true;
  } catch (e) {}
};

onMounted(() => {
  fetchData();
});
</script>
