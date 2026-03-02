<template>
  <div class="app-container" style="padding: 20px">
    <a-card :bordered="false">
      <!-- Search Form -->
      <a-form layout="inline" style="margin-bottom: 20px">
        <a-form-item label="关键词">
          <a-input
            v-model:value="queryParams.keyword"
            placeholder="标题/内容"
            allowClear
          />
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

      <!-- Post Table -->
      <a-table
        :columns="columns"
        :data-source="postList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'title'">
            <a @click="handleDetail(record)">{{ record.title }}</a>
            <a-tag
              color="green"
              v-if="record.isFeatured"
              style="margin-left: 8px"
              >推荐</a-tag
            >
          </template>
          <template v-if="column.key === 'status'">
            <a-tag v-if="record.status === 'PENDING'" color="orange"
              >待审核</a-tag
            >
            <a-tag v-else-if="record.status === 'ACTIVE'" color="green"
              >已发布</a-tag
            >
            <a-tag v-else-if="record.status === 'REJECTED'" color="red"
              >已驳回</a-tag
            >
          </template>
          <template v-if="column.key === 'action'">
            <a @click="handleDetail(record)">详情</a>
            <a-divider type="vertical" />
            <a-popconfirm
              v-if="record.status === 'PENDING'"
              title="确认通过审核?"
              @confirm="handleAudit(record, true)"
            >
              <a style="color: #52c41a">通过</a>
            </a-popconfirm>
            <a-divider type="vertical" v-if="record.status === 'PENDING'" />
            <a
              v-if="record.status === 'PENDING'"
              @click="handleReject(record)"
              style="color: #ff4d4f"
              >驳回</a
            >
            <a-divider type="vertical" />
            <a @click="handleDelete(record)" style="color: #ff4d4f">删除</a>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Detail Drawer -->
    <a-drawer
      title="帖子审核"
      placement="right"
      :visible="detailVisible"
      @close="detailVisible = false"
      width="700px"
    >
      <div v-if="currentPost">
        <h2>{{ currentPost.title }}</h2>
        <div style="margin-bottom: 16px; color: #999">
          <a-avatar :src="currentPost.authorAvatar" size="small" />
          {{ currentPost.authorName }}
          <span style="margin-left: 16px">{{ currentPost.createdAt }}</span>
        </div>
        <div v-html="currentPost.content" class="post-content"></div>
        <a-divider />
        <div v-if="currentPost.status === 'PENDING'" style="text-align: right">
          <a-space>
            <a-button @click="handleReject(currentPost)" danger>驳回</a-button>
            <a-button type="primary" @click="handleAudit(currentPost, true)"
              >通过</a-button
            >
          </a-space>
        </div>
      </div>
    </a-drawer>

    <!-- Reject Modal -->
    <a-modal v-model:visible="rejectOpen" title="驳回原因" @ok="submitReject">
      <a-form :model="rejectForm">
        <a-form-item>
          <a-textarea
            v-model:value="rejectForm.reason"
            placeholder="请输入驳回原因"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { SearchOutlined, ReloadOutlined } from "@ant-design/icons-vue";
import { message, Modal } from "ant-design-vue";
import { listPost, getPost, auditPost, delPost } from "@/api/knowledge-post";

const loading = ref(false);
const postList = ref([]);
const detailVisible = ref(false);
const currentPost = ref<any>(null);
const rejectOpen = ref(false);
const rejectForm = reactive({ id: 0, reason: "" });

const queryParams = reactive({
  keyword: "",
  status: "PENDING", // Default to pending for audit page? Or show all for admin? Requirement says "待审核列表"
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
  { title: "标题", dataIndex: "title", key: "title" },
  { title: "发布者", dataIndex: "authorName", key: "authorName", width: 120 },
  { title: "发布时间", dataIndex: "createdAt", key: "createdAt", width: 180 },
  { title: "状态", key: "status", width: 100 },
  { title: "操作", key: "action", width: 200 },
];

const getList = () => {
  loading.value = true;
  listPost(queryParams)
    .then((res) => {
      postList.value = res.data.records;
      pagination.total = res.data.total;
    })
    .finally(() => {
      loading.value = false;
    });
};

const handleQuery = () => {
  queryParams.page = 1;
  getList();
};

const resetQuery = () => {
  queryParams.keyword = "";
  queryParams.status = "PENDING";
  handleQuery();
};

const handleTableChange = (pag: any) => {
  queryParams.page = pag.current;
  queryParams.size = pag.pageSize;
  getList();
};

const handleDetail = (record: any) => {
  getPost(record.id).then((res) => {
    currentPost.value = res.data;
    detailVisible.value = true;
  });
};

const handleAudit = (record: any, pass: boolean) => {
  auditPost(record.id, pass).then(() => {
    message.success("审核通过");
    detailVisible.value = false;
    getList();
  });
};

const handleReject = (record: any) => {
  rejectForm.id = record.id;
  rejectForm.reason = "";
  rejectOpen.value = true;
};

const submitReject = () => {
  if (!rejectForm.reason) {
    message.warning("请输入驳回原因");
    return;
  }
  auditPost(rejectForm.id, false, rejectForm.reason).then(() => {
    message.success("已驳回");
    rejectOpen.value = false;
    detailVisible.value = false;
    getList();
  });
};

const handleDelete = (record: any) => {
  Modal.confirm({
    title: "确认删除?",
    content: `确认删除帖子 "${record.title}" 吗?`,
    onOk: () => {
      delPost(record.id).then(() => {
        message.success("删除成功");
        getList();
      });
    },
  });
};

onMounted(() => {
  getList();
});
</script>

<style scoped>
.post-content img {
  max-width: 100%;
}
</style>
