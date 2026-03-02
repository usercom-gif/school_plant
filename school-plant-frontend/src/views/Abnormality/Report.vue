<template>
  <div class="max-w-2xl mx-auto">
    <a-card title="植物异常上报" class="shadow-sm">
      <a-form layout="vertical" :model="formState" @finish="handleSubmit">
        <a-form-item label="关联植物" name="plantId" :rules="[{ required: true, message: '请选择关联植物' }]">
          <a-select v-model:value="formState.plantId" placeholder="选择您认养的植物" :options="adoptedPlants" />
        </a-form-item>

        <a-form-item label="异常类型" name="type" :rules="[{ required: true, message: '请选择异常类型' }]">
          <a-select v-model:value="formState.type" placeholder="选择类型">
            <a-select-option value="病害">病害 (叶斑、腐烂等)</a-select-option>
            <a-select-option value="虫害">虫害 (蚜虫、红蜘蛛等)</a-select-option>
            <a-select-option value="缺素">缺素 (黄叶、生长停滞)</a-select-option>
            <a-select-option value="外伤">物理外伤 (折断、倒伏)</a-select-option>
            <a-select-option value="其他">其他</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="异常描述" name="desc" :rules="[{ required: true, message: '请描述具体情况' }]">
          <a-textarea v-model:value="formState.desc" :rows="4" placeholder="请详细描述植物的异常症状..." />
        </a-form-item>

        <a-form-item label="现场照片" name="images">
          <div class="flex flex-col gap-2">
            <input type="file" accept="image/*" multiple @change="handleFileChange" />
            <div v-if="previewImages.length" class="flex gap-2 mt-2 flex-wrap">
              <img v-for="(src, idx) in previewImages" :key="idx" :src="src" class="w-20 h-20 object-cover rounded border" />
            </div>
            <div class="text-xs text-gray-500 mt-1">上传照片后，系统将尝试自动分析并给出建议。</div>
          </div>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" block>提交上报</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- AI Analysis Result Modal -->
    <a-modal v-model:open="resultModalVisible" title="上报成功 & AI分析结果" :footer="null">
      <a-result status="success" title="上报成功！" sub-title="管理员已收到您的报告，将尽快分派处理。">
        <template #extra>
          <div class="bg-blue-50 p-4 rounded text-left border border-blue-100">
            <h4 class="font-bold text-blue-800 mb-2">🤖 AI 初步诊断建议：</h4>
            <p class="text-blue-700 whitespace-pre-wrap">{{ aiAnalysisResult }}</p>
          </div>
          <div class="mt-4">
            <a-button type="primary" @click="router.push('/user/my-abnormalities')">查看我的上报记录</a-button>
          </div>
        </template>
      </a-result>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { reportAbnormality } from '@/api/abnormality';
import { getMyApplications } from '@/api/adoption'; // Import getMyApplications

const router = useRouter();
const loading = ref(false);
const resultModalVisible = ref(false);
const aiAnalysisResult = ref('');
const adoptedPlants = ref<any[]>([]); // Store adopted plants

const formState = reactive({
  plantId: undefined,
  type: undefined,
  desc: '',
  images: [] as File[],
});

// Fetch adopted plants
onMounted(async () => {
  try {
    const res: any = await getMyApplications({ page: 1, size: 100 });
    // Filter for approved/passed applications to get valid plants
    adoptedPlants.value = res.records
      .filter((app: any) => app.status === 'PASS' || app.status === 'ADOPTED') // Adjust based on actual status
      .map((app: any) => ({
        label: `${app.plantName} (ID: ${app.plantId})`,
        value: app.plantId
      }));
      
      // Also consider plants where status is PASS but maybe frontend displays differently
      // Let's check typical status values. Based on previous turns, 'PASS' is used for approval.
  } catch (e) {
    console.error(e);
  }
});

const previewImages = ref<string[]>([]);

const handleFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement;
  if (target.files) {
    formState.images = Array.from(target.files);
    previewImages.value = [];
    formState.images.forEach(file => {
      const reader = new FileReader();
      reader.onload = (e) => {
        if (e.target?.result) {
          previewImages.value.push(e.target.result as string);
        }
      };
      reader.readAsDataURL(file);
    });
  }
};

const handleSubmit = async () => {
  if (!formState.plantId) return;
  
  loading.value = true;
  try {
    const res = await reportAbnormality({
      plantId: formState.plantId,
      type: formState.type!,
      desc: formState.desc,
      images: formState.images
    });
    
    // API returns AI analysis string directly
    aiAnalysisResult.value = res || '暂无AI建议';
    resultModalVisible.value = true;
    
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};
</script>
