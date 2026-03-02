<template>
  <div class="p-6">
    <div class="mb-4 flex justify-between items-center">
      <h1 class="text-2xl font-bold">知识共享审核</h1>
      <div class="flex gap-4">
        <a-input-search
          v-model:value="queryParams.keyword"
          placeholder="搜索标题"
          @search="handleSearch"
          class="w-64"
        />
        <a-select v-model:value="queryParams.status" @change="handleSearch" class="w-32">
          <a-select-option value="">全部状态</a-select-option>
          <a-select-option value="PENDING">待审核</a-select-option>
          <a-select-option value="ACTIVE">已发布</a-select-option>
          <a-select-option value="REJECTED">已拒绝</a-select-option>
        </a-select>
      </div>
    </div>

    <a-table
      :columns="columns"
      :dataSource="posts"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
      rowKey="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'isFeatured'">
           <a-switch 
            :checked="!!record.isFeatured" 
            @change="(checked: any) => handleFeatureToggle(record, checked as boolean)"
            checked-children="是" 
            un-checked-children="否"
          />
        </template>
        <template v-if="column.key === 'action'">
          <div class="flex gap-2">
            <a-button type="link" size="small" @click="openDetail(record)">查看</a-button>
            <template v-if="record.status === 'PENDING'">
              <a-button type="link" size="small" class="text-green-600" @click="handleAudit(record, true)">通过</a-button>
              <a-button type="link" size="small" danger @click="handleAudit(record, false)">拒绝</a-button>
            </template>
            <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
          </div>
        </template>
      </template>
    </a-table>

    <!-- Detail Drawer -->
    <a-drawer
      v-model:visible="detailVisible"
      title="帖子详情"
      width="600"
    >
      <div v-if="currentPost">
        <h2 class="text-xl font-bold mb-2">{{ currentPost.title }}</h2>
        <div class="text-gray-500 mb-4">
          作者: {{ currentPost.authorName }} | 时间: {{ formatDate(currentPost.createdAt) }}
        </div>
        <div class="whitespace-pre-wrap mb-8">{{ currentPost.content }}</div>
        
        <div v-if="currentPost.status === 'PENDING'" class="flex gap-4 border-t pt-4">
           <a-button type="primary" @click="handleAudit(currentPost, true)">通过审核</a-button>
           <a-button danger @click="handleAudit(currentPost, false)">拒绝发布</a-button>
        </div>
      </div>
    </a-drawer>
    
    <!-- Reject Reason Modal -->
    <a-modal
      v-model:visible="rejectVisible"
      title="拒绝理由"
      @ok="submitReject"
    >
      <a-textarea v-model:value="rejectReason" placeholder="请输入拒绝理由" :rows="4" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { listPosts, deletePost, auditPost, toggleFeature } from '@/api/knowledge';
import type { KnowledgePostVO, KnowledgePostQueryRequest } from '@/api/knowledge';
import dayjs from 'dayjs';

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true },
  { title: '作者', dataIndex: 'authorName', key: 'authorName', width: 120 },
  { title: '标签', dataIndex: 'tag', key: 'tag', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '推荐', dataIndex: 'isFeatured', key: 'isFeatured', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180, customRender: ({ text }: { text: string }) => formatDate(text) },
  { title: '操作', key: 'action', width: 200 },
];

const loading = ref(false);
const posts = ref<KnowledgePostVO[]>([]);
const total = ref(0);
const queryParams = reactive<KnowledgePostQueryRequest>({
  page: 1,
  size: 10,
  keyword: '',
  status: '' // Admin sees all
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true
});

const detailVisible = ref(false);
const currentPost = ref<KnowledgePostVO | null>(null);

const rejectVisible = ref(false);
const rejectReason = ref('');
const auditTargetId = ref<number | null>(null);

const fetchPosts = async () => {
  loading.value = true;
  try {
    const res: any = await listPosts(queryParams);
    const pageData = res.records ? res : (res.data || {});
    posts.value = pageData.records || [];
    total.value = pageData.total || 0;
    pagination.total = total.value;
  } catch (error) {
    message.error('加载失败');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  queryParams.page = 1;
  pagination.current = 1;
  fetchPosts();
};

const handleTableChange = (pag: any) => {
  queryParams.page = pag.current;
  queryParams.size = pag.pageSize;
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchPosts();
};

const openDetail = (post: KnowledgePostVO) => {
  currentPost.value = post;
  detailVisible.value = true;
};

const handleAudit = (post: KnowledgePostVO, pass: boolean) => {
  if (pass) {
    Modal.confirm({
      title: '确认通过',
      content: `确认通过帖子 "${post.title}" 吗？`,
      onOk: async () => {
        try {
          await auditPost(post.id, true);
          message.success('审核通过');
          detailVisible.value = false;
          fetchPosts();
        } catch (e) {
          message.error('操作失败');
        }
      }
    });
  } else {
    auditTargetId.value = post.id;
    rejectReason.value = '';
    rejectVisible.value = true;
  }
};

const submitReject = async () => {
  if (!auditTargetId.value) return;
  try {
    await auditPost(auditTargetId.value, false, rejectReason.value);
    message.success('已拒绝');
    rejectVisible.value = false;
    detailVisible.value = false;
    fetchPosts();
  } catch (e) {
    message.error('操作失败');
  }
};

const handleDelete = (post: KnowledgePostVO) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除此帖子吗？',
    okType: 'danger',
    onOk: async () => {
      try {
        await deletePost(post.id);
        message.success('删除成功');
        fetchPosts();
      } catch (e) {
        message.error('删除失败');
      }
    }
  });
};

const handleFeatureToggle = async (post: KnowledgePostVO, checked: boolean) => {
    try {
        await toggleFeature(post.id, checked);
        message.success(checked ? '已设为推荐' : '已取消推荐');
        post.isFeatured = checked ? 1 : 0;
    } catch(e) {
        message.error('操作失败');
        // Revert switch visually if needed, but table refresh handles it eventually
        fetchPosts(); 
    }
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
    'PENDING': '待审核',
    'ACTIVE': '已发布',
    'REJECTED': '已拒绝'
  };
  return map[status] || status;
};

const formatDate = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm');
};

onMounted(() => {
  fetchPosts();
});
</script>
