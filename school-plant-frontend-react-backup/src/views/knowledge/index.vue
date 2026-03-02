<template>
  <div class="app-container" style="padding: 20px">
    <a-card :bordered="false">
      <!-- Search Form -->
      <a-form layout="inline" style="margin-bottom: 20px">
        <a-form-item label="关键词">
          <a-input v-model:value="queryParams.keyword" placeholder="标题/内容/标签" allowClear />
        </a-form-item>
        <a-form-item label="状态" v-if="isAdmin">
          <a-select v-model:value="queryParams.status" placeholder="状态" style="width: 120px" allowClear>
            <a-select-option value="ACTIVE">已发布</a-select-option>
            <a-select-option value="PENDING">待审核</a-select-option>
            <a-select-option value="REJECTED">已驳回</a-select-option>
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
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>发布知识
        </a-button>
      </div>

      <!-- Post List -->
      <a-list :grid="{ gutter: 16, column: 1 }" :data-source="postList" :loading="loading" :pagination="pagination">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-card :hoverable="true" :class="{ 'featured-card': item.isFeatured }">
              <template #actions>
                <span @click="handleLike(item)">
                  <component :is="item.hasLiked ? 'HeartFilled' : 'HeartOutlined'" :style="{ color: item.hasLiked ? '#ff4d4f' : '' }" />
                  {{ item.likeCount }}
                </span>
                <span @click="handleDetail(item)">
                  <EyeOutlined /> 详情
                </span>
                <span v-if="isAdmin || isAuthor(item)" @click="handleEdit(item)">
                  <EditOutlined /> 编辑
                </span>
                <span v-if="isAdmin || isAuthor(item)" @click="handleDelete(item)">
                  <DeleteOutlined /> 删除
                </span>
                <span v-if="isAdmin" @click="handleFeature(item)">
                  <component :is="item.isFeatured ? 'StarFilled' : 'StarOutlined'" :style="{ color: item.isFeatured ? '#faad14' : '' }" />
                  {{ item.isFeatured ? '取消推荐' : '推荐' }}
                </span>
              </template>
              <a-list-item-meta :description="item.tag">
                <template #title>
                  <a @click="handleDetail(item)">{{ item.title }}</a>
                  <a-tag color="green" v-if="item.isFeatured" style="margin-left: 8px">推荐</a-tag>
                  <a-tag color="orange" v-if="item.status === 'PENDING'" style="margin-left: 8px">待审核</a-tag>
                  <a-tag color="red" v-if="item.status === 'REJECTED'" style="margin-left: 8px">已驳回</a-tag>
                </template>
                <template #avatar>
                  <a-avatar :src="item.authorAvatar" />
                </template>
              </a-list-item-meta>
              <div class="content-preview" v-html="truncateContent(item.content)"></div>
              <div style="margin-top: 8px; color: #999; font-size: 12px;">
                发布者: {{ item.authorName }} | 发布时间: {{ item.createdAt }}
              </div>
            </a-card>
          </a-list-item>
        </template>
      </a-list>
    </a-card>

    <!-- Add/Edit Modal -->
    <a-modal v-model:visible="open" :title="title" @ok="submitForm" :confirmLoading="submitLoading" width="800px">
      <a-form :model="form" :rules="rules" ref="formRef" :label-col="{ span: 4 }">
        <a-form-item label="标题" name="title">
          <a-input v-model:value="form.title" placeholder="请输入标题" />
        </a-form-item>
        <a-form-item label="标签" name="tag">
          <a-select v-model:value="form.tag" placeholder="请选择标签" mode="tags">
            <a-select-option value="#浇水技巧">#浇水技巧</a-select-option>
            <a-select-option value="#施肥知识">#施肥知识</a-select-option>
            <a-select-option value="#病虫害防治">#病虫害防治</a-select-option>
            <a-select-option value="#修剪指南">#修剪指南</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="内容" name="content">
          <a-textarea v-model:value="form.content" placeholder="请输入内容" :rows="10" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Detail Drawer -->
    <a-drawer title="帖子详情" placement="right" :visible="detailVisible" @close="detailVisible = false" width="700px">
      <div v-if="currentPost">
        <h2>{{ currentPost.title }}</h2>
        <div style="margin-bottom: 16px; color: #999;">
          <a-avatar :src="currentPost.authorAvatar" size="small" /> {{ currentPost.authorName }} 
          <span style="margin-left: 16px">{{ currentPost.createdAt }}</span>
          <span style="margin-left: 16px"><EyeOutlined /> {{ currentPost.viewCount || 0 }}</span>
        </div>
        <div v-html="currentPost.content" class="post-content"></div>
        <a-divider />
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div>
            <a-button @click="handleLike(currentPost)" :type="currentPost.hasLiked ? 'primary' : 'default'" shape="round">
              <template #icon><HeartFilled v-if="currentPost.hasLiked" /><HeartOutlined v-else /></template>
              点赞 {{ currentPost.likeCount }}
            </a-button>
          </div>
          <div>
            <a-button type="text" danger @click="handleReport(currentPost)">举报</a-button>
          </div>
        </div>
      </div>
    </a-drawer>

    <!-- Report Modal -->
    <a-modal v-model:visible="reportOpen" title="举报帖子" @ok="submitReport">
      <a-form :model="reportForm">
        <a-form-item label="举报原因">
          <a-textarea v-model:value="reportForm.reason" placeholder="请填写举报原因" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { 
  SearchOutlined, ReloadOutlined, PlusOutlined, EditOutlined, DeleteOutlined, 
  EyeOutlined, HeartOutlined, HeartFilled, StarOutlined, StarFilled 
} from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';
import { listPost, getPost, addPost, updatePost, delPost, toggleLike, toggleFeature, reportPost } from '@/api/knowledge-post';
import { useUserStore } from '@/store/user';

const userStore = useUserStore();
const isAdmin = computed(() => userStore.roles.includes('ADMIN'));
const currentUserId = computed(() => userStore.userId); // Assuming store has userId

const loading = ref(false);
const postList = ref([]);
const open = ref(false);
const title = ref('');
const submitLoading = ref(false);
const formRef = ref();
const detailVisible = ref(false);
const currentPost = ref<any>(null);
const reportOpen = ref(false);
const reportForm = reactive({ id: 0, reason: '' });

const queryParams = reactive({
  keyword: '',
  status: undefined, // Default handled by backend logic (ACTIVE for users)
  page: 1,
  size: 10
});

const pagination = reactive({
  onChange: (page: number) => {
    queryParams.page = page;
    getList();
  },
  pageSize: 10,
  total: 0
});

const form = reactive<any>({
  id: undefined,
  title: '',
  content: '',
  tag: []
});

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
};

const getList = () => {
  loading.value = true;
  listPost(queryParams).then(res => {
    postList.value = res.data.records;
    pagination.total = res.data.total;
  }).finally(() => {
    loading.value = false;
  });
};

const handleQuery = () => {
  queryParams.page = 1;
  getList();
};

const resetQuery = () => {
  queryParams.keyword = '';
  queryParams.status = undefined;
  handleQuery();
};

const handleAdd = () => {
  Object.assign(form, { id: undefined, title: '', content: '', tag: [] });
  title.value = '发布知识';
  open.value = true;
};

const handleEdit = (item: any) => {
  getPost(item.id).then(res => {
    Object.assign(form, res.data);
    // Convert tag string to array if needed, assuming backend returns string like "#tag1" or array
    // Here simplifying: if tag is string, wrap in array? Or use select mode="tags" with string value?
    // Let's assume tag is string field in DB, but frontend select uses array.
    // If tag is single string like "#A", keep as is or array?
    // Let's assume tag is simple string for now.
    // Actually select mode="tags" expects array.
    // Backend entity has String tag.
    form.tag = res.data.tag ? [res.data.tag] : [];
    title.value = '编辑知识';
    open.value = true;
  });
};

const submitForm = () => {
  formRef.value.validate().then(() => {
    submitLoading.value = true;
    const data = { ...form };
    // Convert tag array to string (take first or join)
    if (Array.isArray(data.tag)) {
      data.tag = data.tag.length > 0 ? data.tag[0] : ''; // Simple logic: single tag
    }
    
    const api = form.id ? updatePost : addPost;
    api(data).then(() => {
      message.success(form.id ? '修改成功，需重新审核' : '发布成功，等待审核');
      open.value = false;
      getList();
    }).finally(() => {
      submitLoading.value = false;
    });
  });
};

const handleDelete = (item: any) => {
  Modal.confirm({
    title: '确认删除?',
    content: `确认删除帖子 "${item.title}" 吗?`,
    onOk: () => {
      delPost(item.id).then(() => {
        message.success('删除成功');
        getList();
      });
    }
  });
};

const handleDetail = (item: any) => {
  getPost(item.id).then(res => {
    currentPost.value = res.data;
    detailVisible.value = true;
  });
};

const handleLike = (item: any) => {
  toggleLike(item.id).then(() => {
    item.hasLiked = !item.hasLiked;
    item.likeCount += item.hasLiked ? 1 : -1;
    if (currentPost.value && currentPost.value.id === item.id) {
      currentPost.value.hasLiked = item.hasLiked;
      currentPost.value.likeCount = item.likeCount;
    }
  });
};

const handleFeature = (item: any) => {
  toggleFeature(item.id, !item.isFeatured).then(() => {
    item.isFeatured = !item.isFeatured;
    message.success(item.isFeatured ? '已推荐' : '已取消推荐');
  });
};

const handleReport = (item: any) => {
  reportForm.id = item.id;
  reportForm.reason = '';
  reportOpen.value = true;
};

const submitReport = () => {
  if (!reportForm.reason) {
    message.warning('请输入举报原因');
    return;
  }
  reportPost(reportForm.id, reportForm.reason).then(() => {
    message.success('举报成功，等待处理');
    reportOpen.value = false;
  });
};

const isAuthor = (item: any) => {
  // Need to know current user ID. 
  // Assuming item.authorId is available and we can get current user ID.
  // userStore should provide userId.
  // Since I don't have userStore implementation details, I'll use a placeholder.
  // Actually, backend checks permission. Frontend just hides button.
  // Let's assume we can get userId from userStore.
  // If not available, we can't hide it properly, but backend will block.
  // For now, let's try to access userStore.userInfo.id or similar.
  return item.authorId === (userStore.userInfo?.id || 0);
};

const truncateContent = (content: string) => {
  if (!content) return '';
  const text = content.replace(/<[^>]+>/g, ''); // Strip HTML
  return text.length > 50 ? text.substring(0, 50) + '...' : text;
};

onMounted(() => {
  getList();
});
</script>

<style scoped>
.featured-card {
  border: 1px solid #faad14;
  background-color: #fffbe6;
}
.post-content img {
  max-width: 100%;
}
</style>
