<template>
  <div class="app-container">
    <a-card :bordered="false">
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="queryParams" @finish="handleQuery">
        <a-form-item label="操作人ID">
          <a-input v-model:value="queryParams.userId" placeholder="请输入操作人ID" allowClear />
        </a-form-item>
        <a-form-item label="模块">
          <a-select v-model:value="queryParams.module" placeholder="请选择模块" style="width: 150px" allowClear>
            <a-select-option value="ROLE">角色管理</a-select-option>
            <a-select-option value="PLANT">植物管理</a-select-option>
            <a-select-option value="ADOPTION">认养申请</a-select-option>
            <a-select-option value="TASK">任务模板</a-select-option>
            <a-select-option value="KNOWLEDGE">知识共享</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="操作类型">
          <a-select v-model:value="queryParams.operationType" placeholder="请选择类型" style="width: 150px" allowClear>
            <a-select-option value="INSERT">新增</a-select-option>
            <a-select-option value="UPDATE">修改</a-select-option>
            <a-select-option value="DELETE">删除</a-select-option>
            <a-select-option value="AUDIT">审核</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="结果">
          <a-select v-model:value="queryParams.operationResult" placeholder="请选择结果" style="width: 120px" allowClear>
            <a-select-option value="SUCCESS">成功</a-select-option>
            <a-select-option value="FAILURE">失败</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="时间范围">
          <a-range-picker v-model:value="dateRange" show-time format="YYYY-MM-DD HH:mm:ss" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">查询</a-button>
          <a-button style="margin-left: 8px" @click="resetQuery">重置</a-button>
        </a-form-item>
      </a-form>

      <!-- 日志列表 -->
      <a-table
        style="margin-top: 24px"
        :columns="columns"
        :data-source="logList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operationResult'">
            <a-tag :color="record.operationResult === 'SUCCESS' ? 'green' : 'red'">
              {{ record.operationResult === 'SUCCESS' ? '成功' : '失败' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'executionTime'">
            {{ record.executionTime ? record.executionTime + 'ms' : '-' }}
          </template>
          <template v-if="column.key === 'action'">
            <a @click="handleDetail(record)">详情</a>
          </template>
        </template>
      </a-table>

      <!-- 详情弹窗 -->
      <a-modal v-model:visible="detailVisible" title="日志详情" width="800px" :footer="null">
        <a-descriptions bordered :column="1">
          <a-descriptions-item label="日志ID">{{ currentLog.id }}</a-descriptions-item>
          <a-descriptions-item label="操作模块">{{ currentLog.module }} / {{ currentLog.operationType }}</a-descriptions-item>
          <a-descriptions-item label="操作描述">{{ currentLog.operationDesc }}</a-descriptions-item>
          <a-descriptions-item label="操作人员">
            ID: {{ currentLog.userId }} / 姓名: {{ currentLog.operatorName }} / 角色: {{ currentLog.operatorRole }}
          </a-descriptions-item>
          <a-descriptions-item label="请求信息">
            IP: {{ currentLog.ipAddress }} <br/>
            UA: {{ currentLog.userAgent }}
          </a-descriptions-item>
          <a-descriptions-item label="操作状态">
            <a-tag :color="currentLog.operationResult === 'SUCCESS' ? 'green' : 'red'">
              {{ currentLog.operationResult === 'SUCCESS' ? '成功' : '失败' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="执行耗时">{{ currentLog.executionTime }} ms</a-descriptions-item>
          <a-descriptions-item label="操作时间">{{ currentLog.createdAt }}</a-descriptions-item>
          <a-descriptions-item label="错误信息" v-if="currentLog.errorMsg">
            <pre style="color: red; max-height: 200px; overflow: auto;">{{ currentLog.errorMsg }}</pre>
          </a-descriptions-item>
          <a-descriptions-item label="操作内容">
            <pre style="max-height: 300px; overflow: auto; background: #f5f5f5; padding: 10px;">{{ formatJson(currentLog.operationContent) }}</pre>
          </a-descriptions-item>
        </a-descriptions>
      </a-modal>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { listLogs } from '@/api/log';
import dayjs from 'dayjs';

// 查询参数
const queryParams = reactive({
  userId: undefined,
  module: undefined,
  operationType: undefined,
  operationResult: undefined,
});
const dateRange = ref([]);

// 列表数据
const loading = ref(false);
const logList = ref([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
});

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '模块', dataIndex: 'module', width: 100 },
  { title: '操作类型', dataIndex: 'operationType', width: 100 },
  { title: '操作描述', dataIndex: 'operationDesc', ellipsis: true },
  { title: '操作人', dataIndex: 'operatorName', width: 120 },
  { title: 'IP地址', dataIndex: 'ipAddress', width: 120 },
  { title: '状态', key: 'operationResult', width: 80 },
  { title: '耗时', key: 'executionTime', width: 80 },
  { title: '操作时间', dataIndex: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 80, fixed: 'right' },
];

// 详情弹窗
const detailVisible = ref(false);
const currentLog = ref<any>({});

// 查询方法
const handleQuery = () => {
  pagination.current = 1;
  fetchData();
};

const resetQuery = () => {
  queryParams.userId = undefined;
  queryParams.module = undefined;
  queryParams.operationType = undefined;
  queryParams.operationResult = undefined;
  dateRange.value = [];
  handleQuery();
};

const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      ...queryParams,
      page: pagination.current,
      size: pagination.pageSize,
      startTime: dateRange.value?.[0] ? dayjs(dateRange.value[0]).format('YYYY-MM-DD HH:mm:ss') : undefined,
      endTime: dateRange.value?.[1] ? dayjs(dateRange.value[1]).format('YYYY-MM-DD HH:mm:ss') : undefined,
    };
    const res = await listLogs(params);
    logList.value = res.data.records;
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

const handleDetail = (record: any) => {
  currentLog.value = record;
  detailVisible.value = true;
};

const formatJson = (jsonStr: string) => {
  try {
    if (!jsonStr) return '';
    return JSON.stringify(JSON.parse(jsonStr), null, 2);
  } catch (e) {
    return jsonStr;
  }
};

onMounted(() => {
  fetchData();
});
</script>
