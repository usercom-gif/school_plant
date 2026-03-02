<template>
  <div class="p-6">
    <!-- Header / Filter Area -->
    <div class="mb-6 flex flex-col md:flex-row justify-between items-center gap-4">
      <div class="flex items-center gap-4 w-full md:w-auto">
        <h1 class="text-2xl font-bold text-gray-800 m-0">知识共享社区</h1>
        <a-radio-group v-model:value="viewMode" @change="handleViewModeChange" button-style="solid">
          <a-radio-button value="community">社区广场</a-radio-button>
          <a-radio-button value="my">我的发布</a-radio-button>
        </a-radio-group>
        <a-tag color="blue">共 {{ total }} 篇帖子</a-tag>
      </div>
      <div class="flex items-center gap-4 w-full md:w-auto">
        <a-input-search
          v-model:value="queryParams.keyword"
          placeholder="搜索标题或内容..."
          enter-button
          @search="handleSearch"
          class="w-64"
        />
        <a-select
          v-if="viewMode === 'my'"
          v-model:value="queryParams.status"
          placeholder="筛选状态"
          allow-clear
          class="w-32"
          @change="handleSearch"
        >
          <a-select-option value="">全部状态</a-select-option>
          <a-select-option value="ACTIVE">已发布</a-select-option>
          <a-select-option value="PENDING">审核中</a-select-option>
          <a-select-option value="REJECTED">已拒绝</a-select-option>
        </a-select>
        <a-select
          v-model:value="queryParams.tag"
          placeholder="筛选标签"
          allow-clear
          class="w-32"
          @change="handleSearch"
        >
          <a-select-option value="种植技巧">种植技巧</a-select-option>
          <a-select-option value="病虫害防治">病虫害防治</a-select-option>
          <a-select-option value="植物科普">植物科普</a-select-option>
          <a-select-option value="养护心得">养护心得</a-select-option>
          <a-select-option value="校园植物">校园植物</a-select-option>
        </a-select>
        <a-button type="primary" @click="openCreateModal">
          <template #icon><PlusOutlined /></template>
          发布帖子
        </a-button>
      </div>
    </div>

    <!-- Post List -->
    <a-spin :spinning="loading">
      <div v-if="posts.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <a-card
          v-for="post in posts"
          :key="post.id"
          hoverable
          class="flex flex-col h-full rounded-lg shadow-sm hover:shadow-md transition-shadow"
          :bodyStyle="{ flex: 1, display: 'flex', flexDirection: 'column' }"
        >
          <template #actions>
            <span @click.stop="handleLike(post)" :class="{ 'text-red-500': post.hasLiked }">
              <LikeOutlined v-if="!post.hasLiked" />
              <LikeFilled v-else />
              <span class="ml-1">{{ post.likeCount }}</span>
            </span>
            <span @click.stop="openDetail(post)">
              <EyeOutlined />
              <span class="ml-1">查看</span>
            </span>
            <span @click.stop="handleReport(post)">
              <WarningOutlined />
              <span class="ml-1">举报</span>
            </span>
            <span v-if="isAuthor(post)" @click.stop="handleDelete(post)">
              <DeleteOutlined />
              <span class="ml-1">删除</span>
            </span>
          </template>
          
          <a-card-meta>
            <template #title>
              <div class="flex justify-between items-start">
                <a class="text-lg font-medium text-gray-800 hover:text-blue-600 truncate" @click="openDetail(post)" :title="post.title">
                  {{ post.title }}
                </a>
                <a-tag v-if="post.isFeatured" color="gold" class="ml-2 shrink-0">精选</a-tag>
              </div>
            </template>
            <template #avatar>
              <a-avatar :src="post.authorAvatar || undefined" :style="{ backgroundColor: '#f56a00' }">
                {{ post.authorName ? post.authorName.charAt(0) : 'U' }}
              </a-avatar>
            </template>
            <template #description>
              <div class="text-sm text-gray-500 mb-2">
                <span>{{ post.authorName }}</span>
                <a-divider type="vertical" />
                <span>{{ formatDate(post.createdAt) }}</span>
                <template v-if="viewMode === 'my'">
                  <a-divider type="vertical" />
                  <a-tag :color="getStatusColor(post.status)">{{ getStatusText(post.status) }}</a-tag>
                </template>
              </div>
              <div class="h-20 overflow-hidden text-gray-600 text-sm leading-relaxed mb-4 relative">
                 {{ post.content }}
                 <div class="absolute bottom-0 left-0 w-full h-8 bg-gradient-to-t from-white to-transparent"></div>
              </div>
              <div class="flex flex-wrap gap-1 mt-auto">
                <a-tag v-if="post.tag" color="blue">{{ post.tag }}</a-tag>
              </div>
            </template>
          </a-card-meta>
        </a-card>
      </div>
      <a-empty v-else description="暂无相关帖子" />
    </a-spin>

    <!-- Pagination -->
    <div class="mt-8 flex justify-end" v-if="total > 0">
      <a-pagination
        v-model:current="queryParams.page"
        v-model:pageSize="queryParams.size"
        :total="total"
        show-size-changer
        @change="handlePageChange"
      />
    </div>

    <!-- Create/Edit Modal -->
    <a-modal
      v-model:visible="formVisible"
      :title="formMode === 'create' ? '发布新帖子' : '编辑帖子'"
      @ok="handleFormSubmit"
      :confirmLoading="submitting"
      width="600px"
    >
      <a-form :model="formState" layout="vertical" ref="formRef">
        <a-form-item label="标题" name="title" :rules="[{ required: true, message: '请输入标题' }]">
          <a-input v-model:value="formState.title" placeholder="请输入帖子标题" show-count :maxlength="100" />
        </a-form-item>
        <a-form-item label="标签" name="tag">
           <a-select v-model:value="formState.tag" placeholder="选择标签" allow-clear>
            <a-select-option value="种植技巧">种植技巧</a-select-option>
            <a-select-option value="病虫害防治">病虫害防治</a-select-option>
            <a-select-option value="植物科普">植物科普</a-select-option>
            <a-select-option value="养护心得">养护心得</a-select-option>
            <a-select-option value="校园植物">校园植物</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="内容" name="content" :rules="[{ required: true, message: '请输入内容' }]">
          <a-textarea
            v-model:value="formState.content"
            placeholder="分享你的植物知识..."
            :rows="8"
            show-count
            :maxlength="5000"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Detail Drawer -->
    <a-drawer
      v-model:visible="detailVisible"
      title="帖子详情"
      width="720"
      placement="right"
    >
      <div v-if="currentPost">
        <div class="mb-6">
          <h2 class="text-2xl font-bold text-gray-900 mb-4">{{ currentPost.title }}</h2>
          <div class="flex items-center gap-4 text-gray-500 text-sm mb-6">
            <div class="flex items-center gap-2">
              <a-avatar :src="currentPost.authorAvatar || undefined" size="small" :style="{ backgroundColor: '#f56a00' }">
                {{ currentPost.authorName ? currentPost.authorName.charAt(0) : 'U' }}
              </a-avatar>
              <span>{{ currentPost.authorName }}</span>
            </div>
            <span>发布于 {{ formatDate(currentPost.createdAt) }}</span>
            <a-tag v-if="currentPost.tag" color="blue">{{ currentPost.tag }}</a-tag>
          </div>
        </div>
        
        <div class="text-gray-800 leading-loose whitespace-pre-wrap text-base mb-8 min-h-[200px]">
          {{ currentPost.content }}
        </div>

        <a-divider />

        <div class="flex items-center justify-between">
          <div class="flex gap-4">
            <a-button 
              :type="currentPost.hasLiked ? 'primary' : 'default'" 
              shape="round" 
              @click="handleLike(currentPost)"
            >
              <template #icon><LikeOutlined /></template>
              {{ currentPost.likeCount }} 点赞
            </a-button>
            <a-button shape="round" @click="handleReport(currentPost)">
              <template #icon><WarningOutlined /></template>
              举报
            </a-button>
          </div>
          <div v-if="isAuthor(currentPost)">
            <a-button type="link" danger @click="handleDelete(currentPost)">删除帖子</a-button>
          </div>
        </div>
      </div>
    </a-drawer>
    
    <!-- Report Modal -->
    <a-modal
      v-model:visible="reportVisible"
      title="举报帖子"
      @ok="submitReport"
      :confirmLoading="reporting"
    >
      <a-form layout="vertical">
        <a-form-item label="举报理由" required>
          <a-textarea v-model:value="reportReason" placeholder="请描述违规内容..." :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { 
  PlusOutlined, 
  LikeOutlined, 
  LikeFilled, 
  EyeOutlined, 
  WarningOutlined, 
  DeleteOutlined 
} from '@ant-design/icons-vue';
import { listPosts, createPost, deletePost, likePost, reportPost, getPost } from '@/api/knowledge';
import type { KnowledgePostVO, KnowledgePostQueryRequest, KnowledgePostAddRequest } from '@/api/knowledge';
import dayjs from 'dayjs';

// State
const loading = ref(false);
const posts = ref<KnowledgePostVO[]>([]);
const total = ref(0);
const viewMode = ref<'community' | 'my'>('community');
const queryParams = reactive<KnowledgePostQueryRequest>({
  page: 1,
  size: 9,
  keyword: '',
  tag: undefined,
  status: 'ACTIVE', // Default for community
  authorId: undefined
});

const formVisible = ref(false);
const formMode = ref<'create' | 'edit'>('create');
const submitting = ref(false);
const formRef = ref();
const formState = reactive<KnowledgePostAddRequest>({
  title: '',
  content: '',
  tag: undefined
});

const detailVisible = ref(false);
const currentPost = ref<KnowledgePostVO | null>(null);

const reportVisible = ref(false);
const reporting = ref(false);
const reportReason = ref('');
const reportTargetId = ref<number | null>(null);
const currentUserId = ref<number | null>(null);

// Methods
const initUser = () => {
    // Attempt to parse token payload for user ID, or use localStorage if stored
    const token = localStorage.getItem('token');
    if (token) {
        try {
           // Simple JWT decode or use stored user info
           // Assuming satoken stores something or we have user info
           // For now, let's assume we stored 'userInfo' in login
           const userStr = localStorage.getItem('userInfo');
           if (userStr) {
               const user = JSON.parse(userStr);
               currentUserId.value = user.id;
           }
        } catch (e) {}
    }
};

const handleViewModeChange = () => {
    queryParams.page = 1;
    if (viewMode.value === 'my') {
        queryParams.authorId = currentUserId.value || undefined;
        queryParams.status = undefined; // Show all for my posts
    } else {
        queryParams.authorId = undefined;
        queryParams.status = 'ACTIVE';
    }
    fetchPosts();
};

const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    'PENDING': 'orange',
    'ACTIVE': 'green',
    'REJECTED': 'red'
  };
  return map[status] || 'default';
};

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'PENDING': '审核中',
    'ACTIVE': '已发布',
    'REJECTED': '已拒绝'
  };
  return map[status] || status;
};

const fetchPosts = async () => {
  loading.value = true;
  if (viewMode.value === 'my' && !queryParams.status) {
    // If filtering my posts with no specific status, we want ALL statuses.
    // The backend logic now supports this.
  } else if (viewMode.value === 'community') {
    queryParams.status = 'ACTIVE';
  }

  try {
    const res: any = await listPosts(queryParams);
    // Adjust based on actual API response structure (Page<T> usually has records/rows and total)
    // Assuming R<Page<VO>> returns data: { records: [], total: 0 }
    // Check if res is the data directly or wrapped
    const pageData = res.records ? res : (res.data || {}); 
    posts.value = pageData.records || [];
    total.value = pageData.total || 0;
  } catch (error) {
    console.error(error);
    message.error('获取帖子列表失败');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  queryParams.page = 1;
  fetchPosts();
};

const handlePageChange = () => {
  fetchPosts();
};

const openCreateModal = () => {
  formMode.value = 'create';
  formState.title = '';
  formState.content = '';
  formState.tag = undefined;
  formVisible.value = true;
};

const handleFormSubmit = async () => {
  if (!formState.title || !formState.content) {
    message.warning('请填写标题和内容');
    return;
  }
  
  submitting.value = true;
  try {
    await createPost(formState);
    message.success('发布成功');
    formVisible.value = false;
    fetchPosts();
  } catch (error) {
    console.error(error);
    message.error('发布失败');
  } finally {
    submitting.value = false;
  }
};

const openDetail = async (post: KnowledgePostVO) => {
  // Ideally fetch fresh detail
  try {
    const res: any = await getPost(post.id);
    currentPost.value = res; // Or res.data depending on interceptor
    detailVisible.value = true;
  } catch (error) {
    currentPost.value = post; // Fallback to list data
    detailVisible.value = true;
  }
};

const handleLike = async (post: KnowledgePostVO) => {
  try {
    await likePost(post.id);
    // Optimistic update
    post.hasLiked = !post.hasLiked;
    post.likeCount = post.hasLiked ? post.likeCount + 1 : post.likeCount - 1;
    if (currentPost.value && currentPost.value.id === post.id) {
        currentPost.value.hasLiked = post.hasLiked;
        currentPost.value.likeCount = post.likeCount;
    }
  } catch (error) {
    message.error('操作失败');
  }
};

const handleDelete = (post: KnowledgePostVO) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这篇帖子吗？此操作不可恢复。',
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deletePost(post.id);
        message.success('删除成功');
        if (detailVisible.value && currentPost.value?.id === post.id) {
            detailVisible.value = false;
        }
        fetchPosts();
      } catch (error) {
        message.error('删除失败');
      }
    }
  });
};

const handleReport = (post: KnowledgePostVO) => {
  reportTargetId.value = post.id;
  reportReason.value = '';
  reportVisible.value = true;
};

const submitReport = async () => {
  if (!reportReason.value) {
    message.warning('请填写举报理由');
    return;
  }
  if (!reportTargetId.value) return;

  reporting.value = true;
  try {
    await reportPost(reportTargetId.value, reportReason.value);
    message.success('举报已提交，感谢您的反馈');
    reportVisible.value = false;
  } catch (error) {
    message.error('提交失败');
  } finally {
    reporting.value = false;
  }
};

const formatDate = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm');
};

const isAuthor = (post: KnowledgePostVO) => {
    // If we have currentUserId, compare. 
    return currentUserId.value && post.authorId === currentUserId.value;
};

onMounted(() => {
  initUser();
  fetchPosts();
});
</script>

<style scoped>
/* Add scoped styles if needed */
:deep(.ant-card-meta-title) {
    margin-bottom: 8px !important;
}
</style>
