<template>
  <div class="app-container" style="padding: 20px">
    <a-card :bordered="false">
      <!-- Search Form -->
      <a-form layout="inline" style="margin-bottom: 20px">
        <a-form-item label="植物品种">
          <a-select
            v-model:value="queryParams.plantSpecies"
            placeholder="请选择品种"
            style="width: 150px"
            allowClear
            show-search
          >
            <a-select-option v-for="s in speciesOptions" :key="s" :value="s">{{
              s
            }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="任务类型">
          <a-select
            v-model:value="queryParams.taskType"
            placeholder="请选择类型"
            style="width: 120px"
            allowClear
          >
            <a-select-option value="浇水">浇水</a-select-option>
            <a-select-option value="施肥">施肥</a-select-option>
            <a-select-option value="修剪">修剪</a-select-option>
            <a-select-option value="病虫害防治">病虫害防治</a-select-option>
            <a-select-option value="松土">松土</a-select-option>
            <a-select-option value="清理">清理</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="queryParams.status"
            placeholder="状态"
            style="width: 120px"
            allowClear
          >
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleQuery">
            <template #icon><SearchOutlined /></template>查询
          </a-button>
          <a-button style="margin-left: 8px" @click="resetQuery">
            <template #icon><ReloadOutlined /></template>重置
          </a-button>
        </a-form-item>
      </a-form>

      <!-- Action Buttons -->
      <div style="margin-bottom: 16px">
        <a-button
          type="primary"
          @click="handleAdd"
          v-permission="['system:role:manage']"
        >
          <!-- Assuming Admin permission -->
          <template #icon><PlusOutlined /></template>新增
        </a-button>
        <a-button
          type="primary"
          danger
          :disabled="!selectedRowKeys.length"
          style="margin-left: 8px"
          @click="handleBatchDelete"
          v-permission="['system:role:manage']"
        >
          <template #icon><DeleteOutlined /></template>批量删除
        </a-button>
      </div>

      <!-- Table -->
      <a-table
        :columns="columns"
        :data-source="templateList"
        :row-selection="{
          selectedRowKeys: selectedRowKeys,
          onChange: onSelectChange,
        }"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
        :scroll="{ x: 1300 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-switch
              :checked="record.status === 1"
              @change="(val: boolean) => handleStatusChange(record, val)"
              :disabled="!isAdmin"
            />
          </template>
          <template v-if="column.key === 'action'">
            <a @click="handleDetail(record)">详情</a>
            <a-divider type="vertical" />
            <a @click="handleEdit(record)" v-if="isAdmin">编辑</a>
            <a-divider type="vertical" v-if="isAdmin" />
            <a @click="handleCopy(record)" v-if="isAdmin">复制</a>
            <a-divider type="vertical" v-if="isAdmin" />
            <a
              @click="handleDelete(record)"
              style="color: #ff4d4f"
              v-if="isAdmin"
              >删除</a
            >
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Add/Edit Modal -->
    <a-modal
      v-model:visible="open"
      :title="title"
      @ok="submitForm"
      :confirmLoading="submitLoading"
      width="700px"
    >
      <a-form
        :model="form"
        :rules="rules"
        ref="formRef"
        :label-col="{ span: 6 }"
      >
        <a-form-item label="植物品种" name="plantSpecies">
          <a-select
            v-model:value="form.plantSpecies"
            placeholder="请选择品种"
            show-search
          >
            <a-select-option v-for="s in speciesOptions" :key="s" :value="s">{{
              s
            }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="任务类型" name="taskType">
          <a-select v-model:value="form.taskType" placeholder="请选择类型">
            <a-select-option value="浇水">浇水</a-select-option>
            <a-select-option value="施肥">施肥</a-select-option>
            <a-select-option value="修剪">修剪</a-select-option>
            <a-select-option value="病虫害防治">病虫害防治</a-select-option>
            <a-select-option value="松土">松土</a-select-option>
            <a-select-option value="清理">清理</a-select-option>
          </a-select>
        </a-form-item>
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="任务周期(天)" name="frequencyDays">
              <a-input-number
                v-model:value="form.frequencyDays"
                :min="1"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="预计耗时(分)" name="durationMinutes">
              <a-input-number
                v-model:value="form.durationMinutes"
                :min="1"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="季节性" name="seasonality">
          <a-select v-model:value="form.seasonality" placeholder="请选择季节性">
            <a-select-option value="全年">全年</a-select-option>
            <a-select-option value="春季">春季</a-select-option>
            <a-select-option value="夏季">夏季</a-select-option>
            <a-select-option value="秋季">秋季</a-select-option>
            <a-select-option value="冬季">冬季</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="任务描述" name="taskDescription">
          <a-textarea
            v-model:value="form.taskDescription"
            placeholder="请输入任务描述"
            :rows="2"
          />
        </a-form-item>
        <a-form-item label="操作要求" name="operationRequirements">
          <a-textarea
            v-model:value="form.operationRequirements"
            placeholder="请输入操作要求"
            :rows="2"
          />
        </a-form-item>
        <a-form-item label="评分标准" name="scoreStandard">
          <a-textarea
            v-model:value="form.scoreStandard"
            placeholder="请输入评分标准"
            :rows="2"
          />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Detail Drawer -->
    <a-drawer
      title="模板详情"
      placement="right"
      :visible="detailVisible"
      @close="detailVisible = false"
      width="600"
    >
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="模板ID">{{
          currentTemplate.id
        }}</a-descriptions-item>
        <a-descriptions-item label="植物品种">{{
          currentTemplate.plantSpecies
        }}</a-descriptions-item>
        <a-descriptions-item label="任务类型">{{
          currentTemplate.taskType
        }}</a-descriptions-item>
        <a-descriptions-item label="任务描述">{{
          currentTemplate.taskDescription
        }}</a-descriptions-item>
        <a-descriptions-item label="任务周期"
          >{{ currentTemplate.frequencyDays }} 天</a-descriptions-item
        >
        <a-descriptions-item label="预计耗时"
          >{{ currentTemplate.durationMinutes }} 分钟</a-descriptions-item
        >
        <a-descriptions-item label="季节性">{{
          currentTemplate.seasonality
        }}</a-descriptions-item>
        <a-descriptions-item label="操作要求">{{
          currentTemplate.operationRequirements
        }}</a-descriptions-item>
        <a-descriptions-item label="评分标准">{{
          currentTemplate.scoreStandard
        }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="currentTemplate.status === 1 ? 'green' : 'red'">
            {{ currentTemplate.status === 1 ? "启用" : "禁用" }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">{{
          currentTemplate.createdAt
        }}</a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from "vue";
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined,
} from "@ant-design/icons-vue";
import { message, Modal } from "ant-design-vue";
import {
  listTemplate,
  getTemplate,
  addTemplate,
  updateTemplate,
  delTemplate,
  changeTemplateStatus,
} from "@/api/task-template";
import { getAllSpecies } from "@/api/plant";
import { useUserStore } from "@/store/user"; // Assuming user store exists for permission check

const userStore = useUserStore();
// Simple admin check, better use v-permission directive for buttons
const isAdmin = computed(() => {
  return userStore.roles.includes("ADMIN");
});

const loading = ref(false);
const templateList = ref([]);
const speciesOptions = ref<string[]>([]);
const selectedRowKeys = ref<number[]>([]);
const open = ref(false);
const title = ref("");
const submitLoading = ref(false);
const formRef = ref();
const detailVisible = ref(false);
const currentTemplate = ref<any>({});

const queryParams = reactive({
  plantSpecies: undefined,
  taskType: undefined,
  status: undefined,
  page: 1,
  size: 10,
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
});

const columns = [
  { title: "编号", dataIndex: "id", key: "id", width: 80, fixed: "left" },
  {
    title: "植物品种",
    dataIndex: "plantSpecies",
    key: "plantSpecies",
    width: 150,
    fixed: "left",
  },
  { title: "任务类型", dataIndex: "taskType", key: "taskType", width: 120 },
  {
    title: "任务描述",
    dataIndex: "taskDescription",
    key: "taskDescription",
    width: 200,
    ellipsis: true,
  },
  {
    title: "周期(天)",
    dataIndex: "frequencyDays",
    key: "frequencyDays",
    width: 100,
  },
  {
    title: "耗时(分)",
    dataIndex: "durationMinutes",
    key: "durationMinutes",
    width: 100,
  },
  { title: "季节", dataIndex: "seasonality", key: "seasonality", width: 100 },
  { title: "状态", key: "status", width: 100 },
  { title: "创建时间", dataIndex: "createdAt", key: "createdAt", width: 180 },
  { title: "操作", key: "action", width: 250, fixed: "right" },
];

const form = reactive<any>({
  id: undefined,
  plantSpecies: undefined,
  taskType: undefined,
  taskDescription: "",
  frequencyDays: 7,
  durationMinutes: 30,
  seasonality: "全年",
  operationRequirements: "",
  scoreStandard: "",
  status: 1,
});

const rules = {
  plantSpecies: [
    { required: true, message: "请选择植物品种", trigger: "change" },
  ],
  taskType: [{ required: true, message: "请选择任务类型", trigger: "change" }],
  frequencyDays: [
    { required: true, message: "请输入任务周期", trigger: "blur" },
  ],
  taskDescription: [
    { required: true, message: "请输入任务描述", trigger: "blur" },
  ],
};

const getList = () => {
  loading.value = true;
  listTemplate(queryParams)
    .then((res) => {
      templateList.value = res.data.records;
      pagination.total = res.data.total;
    })
    .finally(() => {
      loading.value = false;
    });
};

const loadSpecies = () => {
  getAllSpecies().then((res) => {
    speciesOptions.value = res.data;
  });
};

const handleQuery = () => {
  queryParams.page = 1;
  getList();
};

const resetQuery = () => {
  queryParams.plantSpecies = undefined;
  queryParams.taskType = undefined;
  queryParams.status = undefined;
  handleQuery();
};

const handleTableChange = (pag: any) => {
  queryParams.page = pag.current;
  queryParams.size = pag.pageSize;
  getList();
};

const handleAdd = () => {
  Object.assign(form, {
    id: undefined,
    plantSpecies: undefined,
    taskType: undefined,
    taskDescription: "",
    frequencyDays: 7,
    durationMinutes: 30,
    seasonality: "全年",
    operationRequirements: "",
    scoreStandard: "",
    status: 1,
  });
  title.value = "新增模板";
  open.value = true;
};

const handleEdit = (record: any) => {
  getTemplate(record.id).then((res) => {
    Object.assign(form, res.data);
    title.value = "编辑模板";
    open.value = true;
  });
};

const handleCopy = (record: any) => {
  getTemplate(record.id).then((res) => {
    Object.assign(form, res.data);
    form.id = undefined; // Clear ID to create new
    form.plantSpecies = undefined; // Force user to re-select or confirm (or keep it but risk unique error)
    // Actually user might want to keep species but change type, or change species.
    // Let's keep values but title implies new
    title.value = "复制模板 (请修改品种或类型)";
    open.value = true;
  });
};

const submitForm = () => {
  formRef.value.validate().then(() => {
    submitLoading.value = true;
    const api = form.id ? updateTemplate : addTemplate;
    api(form)
      .then(() => {
        message.success(form.id ? "修改成功" : "新增成功");
        open.value = false;
        getList();
      })
      .finally(() => {
        submitLoading.value = false;
      });
  });
};

const handleDelete = (record: any) => {
  Modal.confirm({
    title: "确认删除?",
    content: `确认删除该模板吗?`,
    onOk: () => {
      delTemplate(String(record.id)).then(() => {
        message.success("删除成功");
        getList();
      });
    },
  });
};

const handleBatchDelete = () => {
  const ids = selectedRowKeys.value.join(",");
  Modal.confirm({
    title: "确认批量删除?",
    content: `确认删除选中的 ${selectedRowKeys.value.length} 个模板吗?`,
    onOk: () => {
      delTemplate(ids).then(() => {
        message.success("批量删除成功");
        selectedRowKeys.value = [];
        getList();
      });
    },
  });
};

const handleDetail = (record: any) => {
  getTemplate(record.id).then((res) => {
    currentTemplate.value = res.data;
    detailVisible.value = true;
  });
};

const handleStatusChange = (record: any, val: boolean) => {
  const newStatus = val ? 1 : 0;
  changeTemplateStatus(record.id, newStatus)
    .then(() => {
      record.status = newStatus;
      message.success("状态更新成功");
    })
    .catch(() => {
      // Revert
    });
};

const onSelectChange = (keys: any[]) => {
  selectedRowKeys.value = keys;
};

onMounted(() => {
  getList();
  loadSpecies();
});
</script>
