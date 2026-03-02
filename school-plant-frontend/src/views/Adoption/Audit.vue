<template>
  <div>
    <!-- Search Bar -->
    <a-card class="mb-4 shadow-sm" :bordered="false">
      <a-form layout="inline" :model="queryParams" @finish="handleSearch">
        <a-form-item label="状态">
          <a-select
            v-model:value="queryParams.status"
            placeholder="审核状态"
            style="width: 120px"
            allow-clear
          >
            <a-select-option value="PENDING">待审核</a-select-option>
            <a-select-option value="APPROVED">已通过</a-select-option>
            <a-select-option value="REJECTED">已驳回</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading"
            >查询</a-button
          >
          <a-button class="ml-2" @click="resetQuery">重置</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- Table Area -->
    <a-card class="shadow-sm" :bordered="false">
      <div class="mb-4 flex justify-between">
        <div class="text-lg font-bold">认养申请审核</div>
      </div>

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
            <a-space v-if="record.status === 'PENDING'">
              <a @click="handleAudit(record, 'PASS')" class="text-green-600"
                >通过</a
              >
              <a-divider type="vertical" />
              <a @click="handleAudit(record, 'REJECT')" class="text-red-500"
                >驳回</a
              >
            </a-space>
            <span v-else class="text-gray-400">已审核</span>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Details Modal -->
    <a-modal v-model:open="detailsModalVisible" title="申请详情" :footer="null">
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="申请人">{{
          currentDetailRecord?.userName
        }}</a-descriptions-item>
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
        <a-descriptions-item label="养护经验/理由">
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

    <!-- Audit Modal -->
    <a-modal
      v-model:open="auditModalVisible"
      :title="auditAction === 'PASS' ? '通过申请' : '驳回申请'"
      @ok="handleAuditSubmit"
      :confirmLoading="auditLoading"
    >
      <a-form layout="vertical">
        <a-form-item label="审核意见">
          <a-textarea
            v-model:value="auditComment"
            :rows="4"
            placeholder="请输入审核意见（选填）"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue";
import { message } from "ant-design-vue";
import {
  getAuditList,
  auditApplication,
  type AdoptionApplication,
  type AdoptionQueryRequest,
} from "@/api/adoption";

// --- State ---
const loading = ref(false);
const dataSource = ref<AdoptionApplication[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
});

const queryParams = reactive<AdoptionQueryRequest>({
  status: "PENDING",
});

// --- Audit Modal State ---
const auditModalVisible = ref(false);
const auditLoading = ref(false);
const currentRecord = ref<AdoptionApplication | null>(null);
const auditAction = ref<"PASS" | "REJECT">("PASS");
const auditComment = ref("");

// --- Details Modal State ---
const detailsModalVisible = ref(false);
const currentDetailRecord = ref<AdoptionApplication | null>(null);

const columns = [
  { title: "ID", dataIndex: "id", width: 60 },
  { title: "申请人", dataIndex: "userName" }, // Backend should populate this
  { title: "申请植物", dataIndex: "plantName" }, // Backend should populate this
  { title: "申请理由", dataIndex: "careExperience", ellipsis: true },
  { title: "驳回原因", dataIndex: "rejectionReason", ellipsis: true },
  { title: "状态", key: "status", width: 100 },
  { title: "申请时间", dataIndex: "createdAt" },
  { title: "操作", key: "action", width: 180 },
];

// --- Methods ---

const viewDetails = (record: AdoptionApplication) => {
  currentDetailRecord.value = record;
  detailsModalVisible.value = true;
};

const getStatusColor = (status: string) => {
  const map: any = {
    PENDING: "orange",
    PASS: "green",
    ADOPTED: "green",
    REJECTED: "red",
    INITIAL_PASS: "cyan",
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
    const res: any = await getAuditList({
      page: pagination.current,
      size: pagination.pageSize,
      ...queryParams,
    });
    dataSource.value = res.records;
    pagination.total = res.total;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const resetQuery = () => {
  queryParams.status = undefined;
  handleSearch();
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchData();
};

const handleAudit = (
  record: AdoptionApplication,
  action: "PASS" | "REJECT",
) => {
  currentRecord.value = record;
  auditAction.value = action;
  auditComment.value = "";
  auditModalVisible.value = true;
};

const handleAuditSubmit = async () => {
  if (!currentRecord.value) return;

  auditLoading.value = true;
  try {
    await auditApplication({
      id: currentRecord.value.id,
      action: auditAction.value,
      comment: auditComment.value,
    });
    message.success("操作成功");
    auditModalVisible.value = false;
    fetchData();
  } catch (error) {
    console.error(error);
  } finally {
    auditLoading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>
