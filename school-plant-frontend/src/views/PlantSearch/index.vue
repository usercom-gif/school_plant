<template>
  <div class="p-4 md:p-8 min-h-screen bg-gray-50">
    <!-- Header Area -->
    <div
      class="mb-8 flex flex-col md:flex-row justify-between items-start md:items-center gap-4"
    >
      <div>
        <h1 class="text-2xl font-bold text-gray-800 m-0">植物信息查询</h1>
        <p class="text-gray-500 mt-1">浏览并认养您心仪的校园植物</p>
      </div>
      <a-button
        type="primary"
        size="large"
        @click="openAddModal"
        class="shadow-md"
      >
        <template #icon><PlusOutlined /></template>
        新增植物
      </a-button>
    </div>

    <!-- Filter Bar -->
    <a-card class="mb-8 shadow-sm rounded-xl border-none" :bordered="false">
      <a-form
        layout="vertical"
        :model="queryParams"
        @finish="handleSearch"
        class="grid grid-cols-1 md:grid-cols-4 gap-4"
      >
        <a-form-item label="植物名称" class="mb-0">
          <a-input
            v-model:value="queryParams.name"
            placeholder="搜索植物名称"
            allow-clear
          >
            <template #prefix
              ><SearchOutlined class="text-gray-400"
            /></template>
          </a-input>
        </a-form-item>
        <a-form-item label="品种" class="mb-0">
          <a-select
            v-model:value="queryParams.species"
            placeholder="选择品种"
            allow-clear
            show-search
          >
            <a-select-option v-for="s in speciesList" :key="s" :value="s">{{
              s
            }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态" class="mb-0">
          <a-select
            v-model:value="queryParams.status"
            placeholder="选择状态"
            allow-clear
          >
            <a-select-option value="AVAILABLE">可认养</a-select-option>
            <a-select-option value="ADOPTED">已认养</a-select-option>
            <a-select-option value="MAINTENANCE">养护中</a-select-option>
            <a-select-option value="HEALTHY">健康</a-select-option>
            <a-select-option value="SICK">生病</a-select-option>
          </a-select>
        </a-form-item>
        <div class="flex items-end gap-2">
          <a-button
            type="primary"
            html-type="submit"
            :loading="loading"
            class="flex-1"
            >搜索</a-button
          >
          <a-button @click="resetQuery">重置</a-button>
        </div>
      </a-form>
    </a-card>

    <!-- Plant Grid -->
    <a-spin :spinning="loading">
      <div
        v-if="dataSource.length > 0"
        class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6"
      >
        <div
          v-for="plant in dataSource"
          :key="plant.id"
          class="group bg-white rounded-xl shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden border border-gray-100 flex flex-col h-full transform hover:-translate-y-1"
        >
          <!-- Image -->
          <div
            class="h-56 overflow-hidden bg-gray-100 relative cursor-pointer"
            @click="openDetail(plant)"
          >
            <img
              :src="getPlantImage(plant)"
              alt="Plant"
              class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
            />
            <div
              class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-10 transition-all duration-300"
            ></div>

            <!-- Status Badge -->
            <div class="absolute top-3 right-3">
              <a-tag
                :color="getStatusColor(plant.status)"
                class="m-0 border-none shadow-sm px-2 py-0.5 text-sm font-medium"
              >
                {{ getStatusLabel(plant.status) }}
              </a-tag>
            </div>

            <!-- Creator Badge (if own) -->
            <div v-if="isOwner(plant)" class="absolute top-3 left-3">
              <a-tag color="purple" class="m-0 border-none shadow-sm">
                <UserOutlined /> 我发布的
              </a-tag>
            </div>
          </div>

          <!-- Content -->
          <div class="p-5 flex-1 flex flex-col">
            <div class="flex justify-between items-start mb-2">
              <h3
                class="text-xl font-bold text-gray-800 hover:text-green-600 transition-colors cursor-pointer"
                @click="openDetail(plant)"
              >
                {{ plant.name }}
              </h3>
            </div>

            <div class="flex flex-wrap gap-2 mb-3">
              <span
                class="text-xs bg-green-50 text-green-700 px-2 py-1 rounded-full font-medium"
              >
                {{ plant.species }}
              </span>
              <span
                class="text-xs bg-blue-50 text-blue-700 px-2 py-1 rounded-full font-medium"
              >
                {{ plant.region || "未知区域" }}
              </span>
            </div>

            <p
              class="text-gray-500 text-sm mb-4 line-clamp-2 flex-1 leading-relaxed"
            >
              {{ plant.description || "暂无描述" }}
            </p>

            <div
              class="space-y-2 text-sm text-gray-600 mb-5 bg-gray-50 p-3 rounded-lg"
            >
              <div class="flex items-center gap-2">
                <EnvironmentOutlined class="text-green-600" />
                <span class="truncate">{{ plant.locationDescription }}</span>
              </div>
              <div class="flex items-center gap-2">
                <UserOutlined class="text-blue-600" />
                <span class="truncate"
                  >发布人: {{ plant.userRealName || "未知" }}</span
                >
              </div>
            </div>

            <!-- Actions -->
            <div class="flex gap-2 mt-auto">
              <!-- Adopt Button -->
              <a-button
                v-if="
                  plant.status === 'AVAILABLE' || plant.status === 'HEALTHY'
                "
                type="primary"
                class="flex-1 bg-green-600 hover:bg-green-500 border-none"
                @click.stop="handleApply(plant)"
              >
                申请认养
              </a-button>
              <a-button v-else disabled class="flex-1">
                {{ getButtonText(plant.status) }}
              </a-button>

              <!-- Manage Actions (Owner/Admin) -->
              <a-dropdown v-if="canManage(plant)">
                <template #overlay>
                  <a-menu>
                    <a-menu-item key="edit" @click="handleEdit(plant)">
                      <EditOutlined /> 编辑信息
                    </a-menu-item>
                    <a-menu-item key="delete" @click="handleDelete(plant)">
                      <span class="text-red-500"
                        ><DeleteOutlined /> 删除植物</span
                      >
                    </a-menu-item>
                  </a-menu>
                </template>
                <a-button class="px-2">
                  <MoreOutlined />
                </a-button>
              </a-dropdown>

              <!-- View Detail Button (if not owner/admin to fill space) -->
              <a-button v-else @click="openDetail(plant)" class="px-3">
                <EyeOutlined />
              </a-button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div
        v-else-if="!loading"
        class="flex flex-col items-center justify-center py-20 bg-white rounded-xl shadow-sm"
      >
        <a-empty description="暂无符合条件的植物" />
        <a-button type="primary" class="mt-4" @click="resetQuery"
          >清除筛选条件</a-button
        >
      </div>

      <!-- Pagination -->
      <div class="mt-8 flex justify-center" v-if="pagination.total > 0">
        <a-pagination
          v-model:current="pagination.current"
          v-model:pageSize="pagination.pageSize"
          :total="pagination.total"
          @change="handlePageChange"
          show-size-changer
          :show-total="(total: number) => `共 ${total} 条记录`"
        />
      </div>
    </a-spin>

    <!-- Application Modal -->
    <a-modal
      v-model:open="applyModalVisible"
      title="申请认养"
      @ok="handleSubmitApplication"
      :confirmLoading="modalLoading"
      width="600px"
    >
      <div
        v-if="selectedPlant"
        class="mb-6 flex gap-4 bg-green-50 p-4 rounded-xl border border-green-100"
      >
        <img
          :src="getPlantImage(selectedPlant)"
          class="w-24 h-24 object-cover rounded-lg shadow-sm"
        />
        <div>
          <div class="font-bold text-lg text-green-800">
            {{ selectedPlant.name }}
          </div>
          <div class="text-sm text-green-600 font-medium mb-1">
            {{ selectedPlant.species }}
          </div>
          <div class="text-xs text-gray-500 flex items-center gap-1">
            <InfoCircleOutlined /> 认养后需定期负责浇水、施肥及上传生长记录。
          </div>
        </div>
      </div>

      <a-form
        layout="vertical"
        ref="applyFormRef"
        :model="applyFormState"
        :rules="applyRules"
      >
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <a-form-item label="认养周期 (月)" name="adoptionPeriodMonths">
            <a-input-number
              v-model:value="applyFormState.adoptionPeriodMonths"
              :min="1"
              :max="12"
              class="w-full"
              addon-after="个月"
            />
          </a-form-item>
          <a-form-item label="联系方式" name="contactPhone">
            <a-input
              v-model:value="applyFormState.contactPhone"
              placeholder="请输入手机号"
            >
              <template #prefix
                ><PhoneOutlined class="text-gray-400"
              /></template>
            </a-input>
          </a-form-item>
        </div>

        <a-form-item
          label="养护经验与承诺"
          name="careExperience"
          help="请详细描述您的经验，这将提高审核通过率"
        >
          <a-textarea
            v-model:value="applyFormState.careExperience"
            :rows="4"
            placeholder="例如：我有两年的多肉养殖经验，承诺每周至少查看植物三次，并按时浇水..."
            show-count
            :maxlength="200"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Plant Detail Modal -->
    <a-modal
      v-model:open="detailVisible"
      title="植物详情"
      width="800px"
      footer=""
    >
      <div v-if="selectedPlant" class="flex flex-col md:flex-row gap-8">
        <div class="w-full md:w-1/2">
          <div class="rounded-xl overflow-hidden shadow-lg mb-4">
            <a-image
              :src="getPlantImage(selectedPlant)"
              width="100%"
              height="300px"
              class="object-cover"
            />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <!-- Thumbnails if multiple images exist (future enhancement) -->
          </div>
        </div>
        <div class="w-full md:w-1/2 flex flex-col gap-4">
          <div>
            <h2 class="text-2xl font-bold text-gray-800 mb-1">
              {{ selectedPlant.name }}
            </h2>
            <p class="text-gray-500">
              {{ selectedPlant.species }} | {{ selectedPlant.family }}
            </p>
          </div>

          <div
            class="grid grid-cols-2 gap-y-2 text-sm text-gray-600 bg-gray-50 p-4 rounded-lg"
          >
            <span
              >状态:
              <a-tag :color="getStatusColor(selectedPlant.status)">{{
                getStatusLabel(selectedPlant.status)
              }}</a-tag></span
            >
            <span>年份: {{ selectedPlant.plantingYear }}</span>
            <span
              >难度:
              <a-rate
                :value="selectedPlant.careDifficulty"
                disabled
                class="text-xs"
            /></span>
            <span>光照: {{ selectedPlant.lightRequirement || "未知" }}</span>
            <span>水分: {{ selectedPlant.waterRequirement || "未知" }}</span>
            <span>周期: {{ selectedPlant.growthCycle || "未知" }}</span>
          </div>

          <div>
            <h4 class="font-bold text-gray-700 mb-1">位置信息</h4>
            <p class="text-gray-600 text-sm">
              {{ selectedPlant.region }} -
              {{ selectedPlant.locationDescription }}
            </p>
          </div>

          <div>
            <h4 class="font-bold text-gray-700 mb-1">养护要点</h4>
            <p class="text-gray-600 text-sm">
              {{ selectedPlant.careTips || "暂无特别说明" }}
            </p>
          </div>

          <div>
            <h4 class="font-bold text-gray-700 mb-1">详细描述</h4>
            <p class="text-gray-600 text-sm leading-relaxed">
              {{ selectedPlant.description }}
            </p>
          </div>

          <div class="mt-auto pt-4 border-t flex items-center justify-between">
            <div class="text-xs text-gray-400">
              发布于 {{ selectedPlant.createdAt }}
              <br />
              发布人: {{ selectedPlant.userRealName }} ({{
                selectedPlant.userPhone
              }})
            </div>
            <a-button
              v-if="
                selectedPlant.status === 'AVAILABLE' ||
                selectedPlant.status === 'HEALTHY'
              "
              type="primary"
              @click="handleApply(selectedPlant)"
            >
              立即认养
            </a-button>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- Add/Edit Plant Modal -->
    <a-modal
      v-model:open="plantFormVisible"
      :title="plantFormMode === 'add' ? '新增植物' : '编辑植物'"
      @ok="handlePlantFormSubmit"
      :confirmLoading="modalLoading"
      width="700px"
    >
      <a-form
        ref="plantFormRef"
        :model="plantFormState"
        :rules="plantRules"
        layout="vertical"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="植物名称" name="name">
              <a-input
                v-model:value="plantFormState.name"
                placeholder="请输入名称"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="品种" name="species">
              <a-input
                v-model:value="plantFormState.species"
                placeholder="请输入品种"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="植物编号" name="plantCode">
              <a-input
                v-model:value="plantFormState.plantCode"
                placeholder="唯一编号"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="科属" name="family">
              <a-input
                v-model:value="plantFormState.family"
                placeholder="例如：菊科"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="所在区域" name="region">
              <a-input
                v-model:value="plantFormState.region"
                placeholder="例如：东校区"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="详细位置" name="locationDescription">
              <a-input
                v-model:value="plantFormState.locationDescription"
                placeholder="例如：图书馆门口花坛"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="养护难度" name="careDifficulty">
              <a-rate v-model:value="plantFormState.careDifficulty" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="种植年份" name="plantingYear">
              <a-input-number
                v-model:value="plantFormState.plantingYear"
                class="w-full"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="光照需求" name="lightRequirement">
              <a-select v-model:value="plantFormState.lightRequirement">
                <a-select-option value="全日照">全日照</a-select-option>
                <a-select-option value="半日照">半日照</a-select-option>
                <a-select-option value="耐阴">耐阴</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="植物图片" name="imageUrls">
          <a-upload
            list-type="picture-card"
            :show-upload-list="false"
            action="/api/common/upload"
            :headers="uploadHeaders"
            @change="handleImageChange"
          >
            <img
              v-if="plantFormState.imageUrls"
              :src="plantFormState.imageUrls"
              alt="avatar"
              class="w-full h-full object-cover"
            />
            <div v-else>
              <plus-outlined />
              <div class="mt-2">上传</div>
            </div>
          </a-upload>
        </a-form-item>

        <a-form-item label="养护要点" name="careTips">
          <a-textarea
            v-model:value="plantFormState.careTips"
            placeholder="简要说明养护注意事项"
            :rows="2"
          />
        </a-form-item>

        <a-form-item label="详细描述" name="description">
          <a-textarea v-model:value="plantFormState.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue";
import {
  EnvironmentOutlined,
  CalendarOutlined,
  SearchOutlined,
  PlusOutlined,
  UserOutlined,
  PhoneOutlined,
  InfoCircleOutlined,
  EditOutlined,
  DeleteOutlined,
  MoreOutlined,
  EyeOutlined,
} from "@ant-design/icons-vue";
import { message, Modal } from "ant-design-vue";
import type { UploadChangeParam } from "ant-design-vue";
import {
  getPlantList,
  getPlantSpecies,
  addPlant,
  updatePlant,
  deletePlant,
  type PlantVO,
  type PlantAddRequest,
} from "@/api/plant";
import { submitApplication, type AdoptionApplyRequest } from "@/api/adoption";
import { getUserProfile } from "@/api/user";

// --- State ---
const loading = ref(false);
const dataSource = ref<PlantVO[]>([]);
const speciesList = ref<string[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 12,
  total: 0,
});

const queryParams = reactive({
  name: "",
  species: undefined,
  status: undefined,
});

// User Info for Permission Check
const currentUserId = ref<number | null>(null);
const isAdmin = ref(false);

// --- Apply Modal State ---
const applyModalVisible = ref(false);
const modalLoading = ref(false);
const selectedPlant = ref<PlantVO | null>(null);
const applyFormRef = ref();
const applyFormState = reactive<AdoptionApplyRequest>({
  plantId: 0,
  adoptionPeriodMonths: 6,
  contactPhone: "",
  careExperience: "",
});

const applyRules = {
  adoptionPeriodMonths: [{ required: true, message: "请输入认养周期" }],
  contactPhone: [
    { required: true, message: "请输入联系方式" },
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确" },
  ],
  careExperience: [
    { required: true, message: "请填写养护经验与承诺" },
    { min: 20, message: "内容不能少于20个字" },
  ],
};

// --- Detail Modal State ---
const detailVisible = ref(false);

// --- Plant Manage Modal State ---
const plantFormVisible = ref(false);
const plantFormMode = ref<"add" | "edit">("add");
const plantFormRef = ref();
const plantFormState = reactive<any>({
  id: undefined,
  plantCode: "",
  name: "",
  species: "",
  family: "",
  locationDescription: "",
  region: "",
  careDifficulty: 3,
  plantingYear: new Date().getFullYear(),
  lightRequirement: "全日照",
  imageUrls: "",
  careTips: "",
  description: "",
});

const plantRules = {
  name: [{ required: true, message: "请输入名称" }],
  species: [{ required: true, message: "请输入品种" }],
  plantCode: [{ required: true, message: "请输入编号" }],
  locationDescription: [{ required: true, message: "请输入位置" }],
};

const token = localStorage.getItem("token");
const uploadHeaders = token ? { satoken: token } : {};

// --- Methods ---

const initUser = () => {
  const role = localStorage.getItem("role");
  isAdmin.value = role === "ADMIN";

  // Try to parse user ID from token or fetch profile if needed
  // Here we fetch profile to get ID reliably
  getUserProfile()
    .then((res: any) => {
      currentUserId.value = res.id;
      // Pre-fill phone for apply
      applyFormState.contactPhone = res.phone;
    })
    .catch(() => {});
};

const isOwner = (plant: PlantVO) => {
  return currentUserId.value && plant.createdBy === currentUserId.value;
};

const canManage = (plant: PlantVO) => {
  return isAdmin.value || isOwner(plant);
};

const getStatusColor = (status: string) => {
  const map: any = {
    HEALTHY: "green",
    SICK: "red",
    MAINTENANCE: "orange",
    ADOPTED: "blue",
    AVAILABLE: "cyan",
  };
  return map[status] || "default";
};

const getStatusLabel = (status: string) => {
  const map: any = {
    HEALTHY: "健康",
    SICK: "生病",
    MAINTENANCE: "养护中",
    ADOPTED: "已认养",
    AVAILABLE: "可认养",
  };
  return map[status] || status;
};

const getPlantImage = (plant: PlantVO) => {
  if (plant.imageUrls) {
    try {
      const parsed = JSON.parse(plant.imageUrls);
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed[0];
      } else if (typeof parsed === "string" && parsed.startsWith("http")) {
        return parsed;
      }
    } catch (e) {
      return plant.imageUrls;
    }
  }
  return "https://via.placeholder.com/400x300?text=No+Image";
};

const getButtonText = (status: string) => {
  if (status === "ADOPTED") return "已被认养";
  if (status === "SICK" || status === "MAINTENANCE") return "养护中";
  return "申请认养";
};

const fetchData = async () => {
  loading.value = true;
  try {
    const res: any = await getPlantList({
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

const fetchSpecies = async () => {
  try {
    const res = await getPlantSpecies();
    speciesList.value = res;
  } catch (e) {}
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const resetQuery = () => {
  queryParams.name = "";
  queryParams.species = undefined;
  queryParams.status = undefined;
  handleSearch();
};

const handlePageChange = (page: number, pageSize: number) => {
  pagination.current = page;
  pagination.pageSize = pageSize;
  fetchData();
};

// --- Detail ---
const openDetail = (plant: PlantVO) => {
  selectedPlant.value = plant;
  detailVisible.value = true;
};

// --- Apply ---
const handleApply = (plant: PlantVO) => {
  // Check if own plant? Usually can't adopt own plant, but logic allows it unless blocked by backend
  selectedPlant.value = plant;
  applyFormState.plantId = plant.id;
  applyFormState.adoptionPeriodMonths = 6;
  applyFormState.careExperience = "";
  // Phone pre-filled in initUser
  applyModalVisible.value = true;
};

const handleSubmitApplication = async () => {
  try {
    await applyFormRef.value.validate();
    modalLoading.value = true;
    await submitApplication(applyFormState);
    message.success("申请提交成功，请等待审核");
    applyModalVisible.value = false;
  } catch (error) {
    console.error(error);
  } finally {
    modalLoading.value = false;
  }
};

// --- Plant Management ---
const openAddModal = () => {
  plantFormMode.value = "add";
  // Reset form
  plantFormState.id = undefined;
  plantFormState.name = "";
  plantFormState.species = "";
  plantFormState.plantCode = "P" + Date.now().toString().slice(-6); // Auto gen simple code
  plantFormState.locationDescription = "";
  plantFormState.region = "";
  plantFormState.careDifficulty = 3;
  plantFormState.plantingYear = new Date().getFullYear();
  plantFormState.lightRequirement = "全日照";
  plantFormState.imageUrls = "";
  plantFormState.careTips = "";
  plantFormState.description = "";

  plantFormVisible.value = true;
};

const handleEdit = (plant: PlantVO) => {
  plantFormMode.value = "edit";
  Object.assign(plantFormState, plant);
  // Handle image url parsing if needed
  plantFormState.imageUrls = getPlantImage(plant); // Simplify to single string for edit
  plantFormVisible.value = true;
};

const handleDelete = (plant: PlantVO) => {
  Modal.confirm({
    title: "确认删除",
    content: `确定要删除 "${plant.name}" 吗？此操作不可恢复。`,
    okType: "danger",
    onOk: async () => {
      try {
        await deletePlant([plant.id]);
        message.success("删除成功");
        fetchData();
      } catch (e) {
        message.error("删除失败");
      }
    },
  });
};

const handleImageChange = (info: UploadChangeParam) => {
  if (info.file.status === "done") {
    const res = info.file.response;
    if (res && res.code === 200) {
      plantFormState.imageUrls = res.data.url;
      message.success("上传成功");
    }
  }
};

const handlePlantFormSubmit = async () => {
  try {
    await plantFormRef.value.validate();
    modalLoading.value = true;

    // Wrap image in JSON array if needed by backend? Backend `ensureJsonArray` handles it.
    // Just send string URL is fine.

    if (plantFormMode.value === "add") {
      await addPlant(plantFormState);
      message.success("添加成功");
    } else {
      await updatePlant(plantFormState);
      message.success("修改成功");
    }
    plantFormVisible.value = false;
    fetchData();
  } catch (e) {
    console.error(e);
  } finally {
    modalLoading.value = false;
  }
};

onMounted(() => {
  initUser();
  fetchData();
  fetchSpecies();
});
</script>

<style scoped>
/* Optional custom transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}
</style>
