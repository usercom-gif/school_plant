<template>
  <div class="app-container" style="padding: 20px">
    <a-card title="认养申请" :bordered="false">
      <a-spin :spinning="loading">
        <div v-if="!hasPlantId" style="text-align: center; padding: 50px">
          <a-result
            status="warning"
            title="请先选择待认养植物"
            sub-title="您需要从植物查询页面选择一株待认养的植物，然后点击“认养申请”按钮进入此页面。"
          >
            <template #extra>
              <a-button type="primary" @click="goToPlantList">返回植物查询页</a-button>
            </template>
          </a-result>
        </div>

        <a-form
          v-else
          :model="form"
          :rules="rules"
          ref="formRef"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 12 }"
        >
          <!-- Plant Info (Read-only) -->
          <a-divider orientation="left">植物信息 (系统自动填充，不可修改)</a-divider>
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="植物编号">
                <a-input v-model:value="plantInfo.plantCode" disabled class="read-only-input" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="植物名称">
                <a-input v-model:value="plantInfo.name" disabled class="read-only-input" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="品种">
                <a-input v-model:value="plantInfo.species" disabled class="read-only-input" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="科属">
                <a-input v-model:value="plantInfo.family" disabled class="read-only-input" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="种植位置">
                <a-input v-model:value="plantInfo.locationDescription" disabled class="read-only-input" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="养护难度">
                <a-rate v-model:value="plantInfo.careDifficulty" disabled />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="生长周期">
                <a-input v-model:value="plantInfo.growthCycle" disabled class="read-only-input" />
              </a-form-item>
            </a-col>
          </a-row>

          <!-- Application Info -->
          <a-divider orientation="left">申请信息</a-divider>
          <a-form-item label="认养周期" name="adoptionPeriodMonths">
            <a-select v-model:value="form.adoptionPeriodMonths" placeholder="请选择认养周期">
              <a-select-option :value="1">1个月</a-select-option>
              <a-select-option :value="3">3个月</a-select-option>
              <a-select-option :value="6">6个月 (一学期)</a-select-option>
              <a-select-option :value="12">12个月 (一学年)</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="联系方式" name="contactPhone">
            <a-input v-model:value="form.contactPhone" placeholder="请输入联系方式" />
          </a-form-item>
          <a-form-item label="认养承诺" name="careExperience">
            <a-textarea
              v-model:value="form.careExperience"
              placeholder="请填写认养承诺，承诺按时完成养护任务... (不少于50字)"
              :rows="6"
              show-count
              :maxlength="500"
            />
          </a-form-item>

          <a-form-item :wrapper-col="{ offset: 6, span: 12 }">
            <a-button type="primary" @click="submit" :loading="submitLoading">提交申请</a-button>
            <a-button style="margin-left: 10px" @click="goToPlantList">取消</a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import { getPlant } from '@/api/plant';
import { submitAdoption } from '@/api/adoption';
import { useUserStore } from '@/store/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const loading = ref(false);
const submitLoading = ref(false);
const hasPlantId = ref(false);
const formRef = ref();

const plantInfo = ref<any>({});
const form = reactive({
  plantId: undefined as number | undefined,
  adoptionPeriodMonths: 6,
  contactPhone: '',
  careExperience: ''
});

const rules = {
  adoptionPeriodMonths: [{ required: true, message: '请选择认养周期', trigger: 'change' }],
  contactPhone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ],
  careExperience: [
    { required: true, message: '请填写认养承诺', trigger: 'blur' },
    { min: 50, message: '认养承诺不能少于50字', trigger: 'blur' }
  ]
};

const init = async () => {
  const plantId = route.query.plantId;
  if (!plantId) {
    hasPlantId.value = false;
    return;
  }
  
  hasPlantId.value = true;
  form.plantId = Number(plantId);
  loading.value = true;
  
  // Load plant info
  try {
    const res = await getPlant(Number(plantId));
    plantInfo.value = res.data;
  } catch (error) {
    message.error('获取植物信息失败');
    hasPlantId.value = false;
  } finally {
    loading.value = false;
  }
};

const submit = () => {
  formRef.value.validate().then(() => {
    submitLoading.value = true;
    submitAdoption(form).then(() => {
      Modal.confirm({
        title: '提交成功',
        content: '认养申请已提交，等待管理员审核',
        okText: '查看我的申请',
        cancelText: '返回植物查询页',
        icon: null,
        onOk: () => {
          router.push('/profile');
        },
        onCancel: () => {
          goToPlantList();
        }
      });
    }).catch(() => {
      // Error handled by request interceptor
    }).finally(() => {
      submitLoading.value = false;
    });
  });
};

const goToPlantList = () => {
  router.push('/plant/query');
};

onMounted(() => {
  init();
});
</script>

<style scoped>
.read-only-input {
  color: rgba(0, 0, 0, 0.85);
  background-color: #f5f5f5;
  cursor: not-allowed;
}
</style>
