<template>
  <div class="app-container">
    <a-card title="植物异常上报" :bordered="false">
      <a-form :model="form" :label-col="{ span: 4 }" :wrapper-col="{ span: 14 }">
        <a-form-item label="选择植物" required>
          <a-select
            v-model:value="form.plantId"
            show-search
            placeholder="输入植物名称搜索"
            :filter-option="false"
            @search="handleSearchPlant"
            :options="plantOptions"
          ></a-select>
        </a-form-item>
        
        <a-form-item label="异常类型" required>
          <a-select v-model:value="form.type" placeholder="请选择异常类型">
            <a-select-option value="病虫害">病虫害</a-select-option>
            <a-select-option value="缺水">缺水</a-select-option>
            <a-select-option value="营养不良">营养不良</a-select-option>
            <a-select-option value="外力损伤">外力损伤</a-select-option>
            <a-select-option value="其他">其他</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="异常描述" required help="不少于20字">
          <a-textarea v-model:value="form.desc" :rows="4" placeholder="请详细描述异常情况" />
        </a-form-item>
        
        <a-form-item label="上传照片" required extra="支持JPG/PNG，单张不超过5MB">
          <a-upload
            v-model:file-list="fileList"
            list-type="picture-card"
            :before-upload="beforeUpload"
            @preview="handlePreview"
          >
            <div v-if="fileList.length < 3">
              <plus-outlined />
              <div style="margin-top: 8px">上传</div>
            </div>
          </a-upload>
        </a-form-item>

        <a-form-item :wrapper-col="{ span: 14, offset: 4 }">
          <a-button type="primary" @click="handleSubmit" :loading="loading">提交并分析</a-button>
        </a-form-item>
      </a-form>

      <!-- AI Result -->
      <a-card v-if="aiResult" title="AI 智能分析结果" style="margin-top: 20px; background: #f6ffed; border-color: #b7eb8f;">
        <template #extra><robot-outlined /></template>
        <p style="white-space: pre-wrap;">{{ aiResult }}</p>
      </a-card>
    </a-card>
    
    <a-modal :visible="previewVisible" :title="previewTitle" :footer="null" @cancel="handleCancel">
      <img alt="example" style="width: 100%" :src="previewImage" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { message, Upload } from 'ant-design-vue';
import { PlusOutlined, RobotOutlined } from '@ant-design/icons-vue';
import { reportAbnormality } from '@/api/abnormality';
import { listPlants } from '@/api/plant'; // Need to search plants

const form = reactive({
  plantId: undefined,
  type: undefined,
  desc: ''
});
const fileList = ref([]);
const loading = ref(false);
const aiResult = ref('');
const plantOptions = ref([]);

const previewVisible = ref(false);
const previewImage = ref('');
const previewTitle = ref('');

// Search Plants
const handleSearchPlant = async (val: string) => {
  if (val) {
    const res = await listPlants({ keyword: val, page: 1, size: 10 });
    plantOptions.value = res.data.records.map((p: any) => ({
      label: p.name + ' (' + p.locationDescription + ')',
      value: p.id
    }));
  }
};

const beforeUpload = (file: any) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    message.error('只能上传 JPG/PNG 文件!');
  }
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    message.error('图片必须小于 5MB!');
  }
  return false; // Prevent auto upload
};

const handleCancel = () => {
  previewVisible.value = false;
  previewTitle.value = '';
};

const handlePreview = async (file: any) => {
  if (!file.url && !file.preview) {
    file.preview = await getBase64(file.originFileObj);
  }
  previewImage.value = file.url || file.preview;
  previewVisible.value = true;
  previewTitle.value = file.name || file.url.substring(file.url.lastIndexOf('/') + 1);
};

const getBase64 = (file: File) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
  });
};

const handleSubmit = async () => {
  if (!form.plantId || !form.type || !form.desc) {
    message.warning('请填写完整信息');
    return;
  }
  if (form.desc.length < 20) {
    message.warning('描述不能少于20字');
    return;
  }
  
  const formData = new FormData();
  formData.append('plantId', String(form.plantId));
  formData.append('type', form.type);
  formData.append('desc', form.desc);
  fileList.value.forEach((file: any) => {
    formData.append('images', file.originFileObj);
  });

  loading.value = true;
  try {
    const res = await reportAbnormality(formData);
    message.success('上报成功');
    aiResult.value = res.data || 'AI 分析中...';
    // Clear form
    form.desc = '';
    fileList.value = [];
  } finally {
    loading.value = false;
  }
};
</script>
