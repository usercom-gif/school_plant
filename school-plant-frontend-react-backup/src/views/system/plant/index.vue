<template>
  <div class="app-container" style="padding: 20px">
    <a-card :bordered="false">
      <!-- Search Form -->
      <a-form layout="inline" style="margin-bottom: 20px">
        <a-form-item label="关键词">
          <a-input
            v-model:value="queryParams.keyword"
            placeholder="名称/品种/位置"
            allowClear
          />
        </a-form-item>
        <a-form-item label="品种">
          <a-select
            v-model:value="queryParams.species"
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
        <a-form-item label="区域">
          <a-select
            v-model:value="queryParams.region"
            placeholder="请选择区域"
            style="width: 120px"
            allowClear
          >
            <a-select-option value="中央校区">中央校区</a-select-option>
            <a-select-option value="东校区">东校区</a-select-option>
            <a-select-option value="西校区">西校区</a-select-option>
            <a-select-option value="南校区">南校区</a-select-option>
            <a-select-option value="北校区">北校区</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="难度">
          <a-select
            v-model:value="queryParams.careDifficulty"
            placeholder="养护难度"
            style="width: 100px"
            allowClear
          >
            <a-select-option :value="1">低</a-select-option>
            <a-select-option :value="2">中</a-select-option>
            <a-select-option :value="3">高</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="queryParams.status"
            placeholder="状态"
            style="width: 120px"
            allowClear
          >
            <a-select-option value="AVAILABLE">待认养</a-select-option>
            <a-select-option value="ADOPTED">已认养</a-select-option>
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
          v-permission="['plant:create']"
        >
          <template #icon><PlusOutlined /></template>新增
        </a-button>
        <a-button
          type="primary"
          ghost
          style="margin-left: 8px"
          @click="handleExport"
          v-permission="['plant:export']"
        >
          <template #icon><DownloadOutlined /></template>导出
        </a-button>
        <a-upload
          name="file"
          :show-upload-list="false"
          :customRequest="handleImport"
          style="margin-left: 8px"
          v-permission="['plant:import']"
        >
          <a-button>
            <template #icon><UploadOutlined /></template>导入
          </a-button>
        </a-upload>
        <a-button
          type="primary"
          danger
          :disabled="!selectedRowKeys.length"
          style="margin-left: 8px"
          @click="handleBatchDelete"
          v-permission="['plant:delete']"
        >
          <template #icon><DeleteOutlined /></template>批量删除
        </a-button>
      </div>

      <!-- Table -->
      <a-table
        :columns="columns"
        :data-source="plantList"
        :row-selection="{
          selectedRowKeys: selectedRowKeys,
          onChange: onSelectChange,
        }"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
        :scroll="{ x: 1600 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'imageUrls'">
            <div v-if="record.imageUrls">
              <a-image
                v-if="parseImages(record.imageUrls).length > 0"
                :width="50"
                :src="getImageUrl(parseImages(record.imageUrls)[0])"
              />
              <span v-else>-</span>
            </div>
            <span v-else>-</span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'AVAILABLE' ? 'green' : 'blue'">
              {{ record.status === "AVAILABLE" ? "待认养" : "已认养" }}
            </a-tag>
          </template>
          <template v-if="column.key === 'careDifficulty'">
            <a-rate :value="record.careDifficulty" disabled count="5" />
          </template>
          <template v-if="column.key === 'action'">
            <a @click="handleDetail(record)">详情</a>
            <!-- Adoption Apply Button for User -->
            <a-divider
              type="vertical"
              v-if="isUser && record.status === 'AVAILABLE'"
            />
            <a
              class="adoption-btn"
              v-if="isUser && record.status === 'AVAILABLE'"
              @click="handleAdoptionApply(record)"
            >
              <a-spin
                size="small"
                v-if="adoptionLoading[record.id]"
                style="margin-right: 4px"
              />
              认养申请
            </a>

            <a-divider type="vertical" v-permission="['plant:edit']" />
            <a @click="handleEdit(record)" v-permission="['plant:edit']"
              >编辑</a
            >
            <a-divider type="vertical" v-permission="['plant:delete']" />
            <a
              @click="handleDelete(record)"
              style="color: #ff4d4f"
              v-permission="['plant:delete']"
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
      width="800px"
    >
      <a-form
        :model="form"
        :rules="rules"
        ref="formRef"
        :label-col="{ span: 6 }"
      >
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="植物编号" name="plantCode">
              <a-input
                v-model:value="form.plantCode"
                placeholder="请输入植物编号"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="植物名称" name="name">
              <a-input v-model:value="form.name" placeholder="请输入植物名称" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="植物品种" name="species">
              <a-input
                v-model:value="form.species"
                placeholder="请输入植物品种"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="科属" name="family">
              <a-input v-model:value="form.family" placeholder="请输入科属" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="位置描述" name="locationDescription">
              <a-input
                v-model:value="form.locationDescription"
                placeholder="请输入位置"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="校园区域" name="region">
              <a-select v-model:value="form.region" placeholder="请选择区域">
                <a-select-option value="中央校区">中央校区</a-select-option>
                <a-select-option value="东校区">东校区</a-select-option>
                <a-select-option value="西校区">西校区</a-select-option>
                <a-select-option value="南校区">南校区</a-select-option>
                <a-select-option value="北校区">北校区</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="养护难度" name="careDifficulty">
              <a-rate v-model:value="form.careDifficulty" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="认养状态" name="status">
              <a-radio-group v-model:value="form.status">
                <a-radio value="AVAILABLE">待认养</a-radio>
                <a-radio value="ADOPTED">已认养</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="生长周期" name="growthCycle">
              <a-input
                v-model:value="form.growthCycle"
                placeholder="如：多年生"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="种植年份" name="plantingYear">
              <a-input-number
                v-model:value="form.plantingYear"
                style="width: 100%"
                placeholder="年份"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="养护要点" name="careTips">
          <a-textarea
            v-model:value="form.careTips"
            placeholder="请输入养护要点"
          />
        </a-form-item>
        <a-form-item label="详细描述" name="description">
          <a-textarea
            v-model:value="form.description"
            placeholder="请输入详细描述"
            :rows="3"
          />
        </a-form-item>
        <a-form-item label="图片" name="imageUrls">
          <ImageUpload v-model:modelValue="form.imageUrls" :limit="5" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Detail Drawer -->
    <a-drawer
      title="植物详情"
      placement="right"
      :visible="detailVisible"
      @close="detailVisible = false"
      width="600"
    >
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="图片">
          <a-image-preview-group
            v-if="
              currentPlant.imageUrls &&
              parseImages(currentPlant.imageUrls).length
            "
          >
            <a-image
              v-for="(url, index) in parseImages(currentPlant.imageUrls)"
              :key="index"
              :width="100"
              :src="getImageUrl(url)"
              style="margin-right: 8px; margin-bottom: 8px"
            />
          </a-image-preview-group>
          <span v-else>暂无图片</span>
        </a-descriptions-item>
        <a-descriptions-item label="植物编号">{{
          currentPlant.plantCode
        }}</a-descriptions-item>
        <a-descriptions-item label="植物名称">{{
          currentPlant.name
        }}</a-descriptions-item>
        <a-descriptions-item label="品种">{{
          currentPlant.species
        }}</a-descriptions-item>
        <a-descriptions-item label="位置">{{
          currentPlant.locationDescription
        }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag
            :color="currentPlant.status === 'AVAILABLE' ? 'green' : 'blue'"
          >
            {{ currentPlant.status === "AVAILABLE" ? "待认养" : "已认养" }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="养护要点">{{
          currentPlant.careTips
        }}</a-descriptions-item>
        <a-descriptions-item label="描述">{{
          currentPlant.description
        }}</a-descriptions-item>
      </a-descriptions>
      <!-- TODO: Show associated info like adopter, tasks -->
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined,
  DownloadOutlined,
  UploadOutlined,
} from "@ant-design/icons-vue";
import { message, Modal } from "ant-design-vue";
import {
  listPlant,
  getPlant,
  addPlant,
  updatePlant,
  delPlant,
  exportPlant,
  importPlant,
  getAllSpecies,
} from "@/api/plant";
import { checkStatus } from "@/api/adoption";
import { useUserStore } from "@/store/user";
import { useRouter } from "vue-router";

import ImageUpload from "@/components/ImageUpload/index.vue";

const router = useRouter();
const userStore = useUserStore();
const isUser =
  userStore.roles.includes("USER") &&
  !userStore.roles.includes("ADMIN") &&
  !userStore.roles.includes("MAINTAINER");

const loading = ref(false);
const plantList = ref([]);
const speciesOptions = ref<string[]>([]);
const selectedRowKeys = ref<number[]>([]);
const open = ref(false);
const title = ref("");
const submitLoading = ref(false);
const formRef = ref();
const detailVisible = ref(false);
const currentPlant = ref<any>({});

const queryParams = reactive({
  keyword: "",
  species: undefined as string | undefined,
  region: undefined as string | undefined,
  status: "AVAILABLE" as string | undefined,
  careDifficulty: undefined as number | undefined,
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
  {
    title: "编号",
    dataIndex: "plantCode",
    key: "plantCode",
    fixed: "left",
    width: 120,
  },
  { title: "名称", dataIndex: "name", key: "name", fixed: "left", width: 150 },
  {
    title: "图片",
    key: "imageUrls",
    width: 100,
    customRender: ({ record }: any) => {
      // Logic handled in bodyCell slot but we can verify here or keep it simple
      return null;
    },
  },
  { title: "品种", dataIndex: "species", key: "species", width: 120 },
  { title: "科属", dataIndex: "family", key: "family", width: 120 },
  { title: "区域", dataIndex: "region", key: "region", width: 100 },
  {
    title: "位置",
    dataIndex: "locationDescription",
    key: "locationDescription",
    width: 200,
  },
  { title: "难度", key: "careDifficulty", width: 150 },
  {
    title: "生长周期",
    dataIndex: "growthCycle",
    key: "growthCycle",
    width: 120,
  },
  {
    title: "种植年份",
    dataIndex: "plantingYear",
    key: "plantingYear",
    width: 100,
  },
  {
    title: "养护要点",
    dataIndex: "careTips",
    key: "careTips",
    width: 200,
    ellipsis: true,
  },
  { title: "数量", dataIndex: "number", key: "number", width: 80 },
  { title: "状态", key: "status", width: 100 },
  { title: "创建时间", dataIndex: "createdAt", key: "createdAt", width: 180 },
  { title: "操作", key: "action", fixed: "right", width: 200 },
];

const form = reactive<any>({
  id: undefined,
  plantCode: "",
  name: "",
  species: "",
  family: "",
  locationDescription: "",
  region: undefined,
  careDifficulty: 1,
  status: "AVAILABLE",
  plantingYear: undefined,
  careTips: "",
  description: "",
  growthCycle: "",
  imageUrls: [], // Array of URLs
});

const rules = {
  plantCode: [{ required: true, message: "请输入植物编号", trigger: "blur" }],
  name: [{ required: true, message: "请输入植物名称", trigger: "blur" }],
  species: [{ required: true, message: "请输入植物品种", trigger: "blur" }],
  locationDescription: [
    { required: true, message: "请输入位置描述", trigger: "blur" },
  ],
  careDifficulty: [
    { required: true, message: "请选择养护难度", trigger: "change" },
  ],
};

const getList = () => {
  loading.value = true;
  listPlant(queryParams)
    .then((res) => {
      plantList.value = res.data.records;
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
  queryParams.keyword = "";
  queryParams.species = undefined;
  queryParams.region = undefined;
  queryParams.status = "AVAILABLE";
  queryParams.careDifficulty = undefined;
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
    plantCode: "",
    name: "",
    species: "",
    family: "",
    locationDescription: "",
    region: undefined,
    careDifficulty: 1,
    status: "AVAILABLE",
    plantingYear: undefined,
    careTips: "",
    description: "",
    growthCycle: "",
  });
  title.value = "新增植物";
  open.value = true;
};

const handleEdit = (record: any) => {
  getPlant(record.id).then((res) => {
    Object.assign(form, res.data);
    if (res.data.plantingYear) {
      // Handle Year object if returned as object, but VO usually serializes to value or string
      // Assuming value
    }
    title.value = "编辑植物";
    open.value = true;
  });
};

const submitForm = () => {
  formRef.value.validate().then(() => {
    submitLoading.value = true;
    const api = form.id ? updatePlant : addPlant;
    api(form)
      .then(() => {
        message.success(form.id ? "修改成功" : "新增成功");
        open.value = false;
        getList();
        loadSpecies(); // Refresh species list
      })
      .finally(() => {
        submitLoading.value = false;
      });
  });
};

const handleDelete = (record: any) => {
  Modal.confirm({
    title: "确认删除?",
    content: `确认删除植物 "${record.name}" 吗?`,
    onOk: () => {
      delPlant(String(record.id)).then(() => {
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
    content: `确认删除选中的 ${selectedRowKeys.value.length} 个植物吗?`,
    onOk: () => {
      delPlant(ids).then(() => {
        message.success("批量删除成功");
        selectedRowKeys.value = [];
        getList();
      });
    },
  });
};

const handleDetail = (record: any) => {
  getPlant(record.id).then((res) => {
    currentPlant.value = res.data;
    detailVisible.value = true;
  });
};

const handleExport = () => {
  exportPlant(queryParams).then((res) => {
    const blob = new Blob([res as any], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });
    const link = document.createElement("a");
    link.href = window.URL.createObjectURL(blob);
    link.download = `植物数据_${new Date().getTime()}.xlsx`;
    link.click();
  });
};

const handleImport = (options: any) => {
  const formData = new FormData();
  formData.append("file", options.file);
  importPlant(formData)
    .then(() => {
      message.success("导入成功");
      getList();
      loadSpecies();
    })
    .catch((err) => {
      message.error("导入失败: " + err.message);
    });
};

const adoptionLoading = ref<Record<number, boolean>>({});

const handleAdoptionApply = (record: any) => {
  if (record.status !== "AVAILABLE") return;

  adoptionLoading.value[record.id] = true;
  checkStatus()
    .then((res) => {
      if (res.data.canAdopt) {
        router.push({ path: "/adoption/apply", query: { plantId: record.id } });
      } else {
        Modal.warning({
          title: "无法申请",
          content: res.data.message,
        });
      }
    })
    .finally(() => {
      adoptionLoading.value[record.id] = false;
    });
};

const getImageUrl = (url: string) => {
  if (!url) return "";
  if (url.startsWith("http")) return url;
  return import.meta.env.VITE_APP_BASE_API + url;
};

const parseImages = (json: string) => {
  try {
    const urls = JSON.parse(json);
    return Array.isArray(urls) ? urls : [];
  } catch (e) {
    return [];
  }
};

const onSelectChange = (keys: any[]) => {
  selectedRowKeys.value = keys;
};

onMounted(() => {
  getList();
  loadSpecies();
});
</script>

<style scoped>
.adoption-btn {
  color: #52c41a;
}
.adoption-btn:hover {
  color: #73d13d;
}
</style>
