<template>
  <div class="component-upload-image">
    <a-upload
      v-model:file-list="fileList"
      list-type="picture-card"
      :before-upload="beforeUpload"
      :customRequest="handleUpload"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      :maxCount="limit"
    >
      <div v-if="fileList.length < limit">
        <plus-outlined />
        <div style="margin-top: 8px">上传图片</div>
      </div>
    </a-upload>
    <a-modal :visible="previewVisible" :title="previewTitle" :footer="null" @cancel="handleCancel">
      <img alt="example" style="width: 100%" :src="previewImage" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { message, Upload, Modal } from 'ant-design-vue';
import type { UploadProps, UploadFile } from 'ant-design-vue';
import { uploadFile } from '@/api/common';

const props = defineProps({
  modelValue: {
    type: [String, Array],
    default: () => []
  },
  limit: {
    type: Number,
    default: 5
  },
  fileSize: {
    type: Number,
    default: 5 // MB
  },
  fileType: {
    type: Array,
    default: () => ['png', 'jpg', 'jpeg', 'gif']
  },
  isShowTip: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['update:modelValue']);

const fileList = ref<UploadFile[]>([]);
const previewVisible = ref(false);
const previewImage = ref('');
const previewTitle = ref('');

// 监听 modelValue 变化，同步到 fileList
watch(() => props.modelValue, (val) => {
  if (val) {
    // 假设 modelValue 是 URL 数组或 JSON 字符串
    let urls = [];
    if (Array.isArray(val)) {
      urls = val;
    } else if (typeof val === 'string') {
      try {
        urls = JSON.parse(val);
      } catch (e) {
        // 尝试作为单个 URL
        if (val.startsWith('http') || val.startsWith('/')) {
          urls = [val];
        }
      }
    }
    
    fileList.value = urls.map((url: string, index: number) => {
      // 检查是否已经是 UploadFile 对象
      return {
        uid: String(index),
        name: url.substring(url.lastIndexOf('/') + 1),
        status: 'done',
        url: url.startsWith('http') ? url : import.meta.env.VITE_APP_BASE_API + url
      };
    });
  } else {
    fileList.value = [];
  }
}, { immediate: true, deep: true });

const getBase64 = (file: File) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
  });
};

const handleCancel = () => {
  previewVisible.value = false;
};

const handlePreview = async (file: UploadFile) => {
  if (!file.url && !file.preview) {
    file.preview = (await getBase64(file.originFileObj as File)) as string;
  }
  previewImage.value = file.url || file.preview as string;
  previewVisible.value = true;
  previewTitle.value = file.name || file.url!.substring(file.url!.lastIndexOf('/') + 1);
};

const beforeUpload = (file: File) => {
  const isTypeOk = props.fileType.some((type: any) => 
    file.type === `image/${type}` || file.name.endsWith(`.${type}`)
  );
  if (!isTypeOk) {
    message.error(`文件格式不正确, 请上传${props.fileType.join("/")}图片格式文件!`);
    return false;
  }
  const isLt = file.size / 1024 / 1024 < props.fileSize;
  if (!isLt) {
    message.error(`上传文件大小不能超过 ${props.fileSize} MB!`);
    return false;
  }
  return true;
};

const handleUpload = ({ file, onSuccess, onError }: any) => {
  const formData = new FormData();
  formData.append('file', file);
  uploadFile(formData).then(res => {
    // 后端返回 { url: "...", fileName: "..." }
    // 更新 fileList 中的当前文件
    // 注意：customRequest 不会自动更新 fileList 状态，需要手动处理或者依靠 fileList 的双向绑定
    // 但这里我们使用 fileList v-model
    
    // 我们需要通知父组件更新 modelValue
    // 构造新的 URL 列表
    const url = res.data.url;
    
    // 更新当前文件的 url
    file.url = url; // 可能是相对路径，用于传给后端
    // file.thumbUrl = url.startsWith('http') ? url : import.meta.env.VITE_APP_BASE_API + url; // 前端展示用绝对路径
    
    onSuccess(res.data);
    updateModelValue();
  }).catch(err => {
    onError(err);
    message.error('上传失败');
  });
};

const handleRemove = (file: UploadFile) => {
  // fileList 会自动更新，我们只需要 emit
  // 等待 fileList 更新后再 emit
  setTimeout(() => {
    updateModelValue();
  }, 100);
};

const updateModelValue = () => {
  // 提取 url
  // 注意：后端通常存储相对路径或完整 URL，这里统一存相对路径（如果后端返回相对路径）
  // 假设后端返回的是 `/profile/xxx.jpg`，前端展示时拼上前缀
  // 如果后端返回完整 URL，则直接存
  const urls = fileList.value.map(file => {
    if (file.response && file.response.data && file.response.data.url) {
      return file.response.data.url;
    }
    // 已经是已有文件
    // 如果 url 包含 base api 前缀，需要去掉吗？视后端存储需求而定
    // 假设后端存的是 `/profile/xxx`，前端展示加了前缀
    // 这里简单处理：如果 url 包含 base api，尝试去掉；或者直接存 url
    // 为了简单，我们存 url。如果 url 是 http 开头，就是完整路径；如果是 / 开头，就是相对路径
    let url = file.url;
    const baseApi = import.meta.env.VITE_APP_BASE_API;
    if (url && url.startsWith(baseApi)) {
      url = url.substring(baseApi.length);
    }
    return url;
  }).filter(url => !!url);
  
  // 返回 JSON 字符串
  emit('update:modelValue', JSON.stringify(urls));
};
</script>

<style scoped>
/* you can add styles here */
</style>
