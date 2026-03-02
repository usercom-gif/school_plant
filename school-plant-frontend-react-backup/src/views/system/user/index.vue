<template>
  <div class="app-container" style="padding: 20px">
    <a-card :bordered="false">
      <!-- Search Form -->
      <a-form layout="inline" style="margin-bottom: 20px">
        <a-form-item label="用户名">
          <a-input v-model:value="queryParams.username" placeholder="请输入用户名" allowClear />
        </a-form-item>
        <a-form-item label="真实姓名">
          <a-input v-model:value="queryParams.realName" placeholder="请输入真实姓名" allowClear />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model:value="queryParams.email" placeholder="请输入邮箱" allowClear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryParams.status" placeholder="用户状态" style="width: 120px" allowClear>
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
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
          <template #icon><PlusOutlined /></template>新增
        </a-button>
        <a-button type="primary" ghost style="margin-left: 8px" @click="handleExport">
          <template #icon><DownloadOutlined /></template>导出
        </a-button>
        <a-upload
          name="file"
          :show-upload-list="false"
          :customRequest="handleImport"
          style="margin-left: 8px"
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
        >
          <template #icon><DeleteOutlined /></template>批量删除
        </a-button>
      </div>

      <!-- Table -->
      <a-table 
        :columns="columns" 
        :data-source="userList" 
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-switch 
              :checked="record.status === 1" 
              @change="(val: any) => handleStatusChange(record, val)" 
            />
          </template>
          <template v-if="column.key === 'action'">
            <a @click="handleEdit(record)">编辑</a>
            <a-divider type="vertical" />
            <a @click="handleDelete(record)" style="color: #ff4d4f">删除</a>
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
    >
      <a-form :model="form" :rules="rules" ref="formRef" :label-col="{ span: 6 }">
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="form.username" :disabled="!!form.id" />
        </a-form-item>
        <a-form-item label="初始密码" name="password" v-if="!form.id">
          <a-input-password v-model:value="form.password" />
        </a-form-item>
        <a-form-item label="真实姓名" name="realName">
          <a-input v-model:value="form.realName" :disabled="!!form.id" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="form.phone" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="form.email" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">正常</a-radio>
            <a-radio :value="0">停用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { 
  SearchOutlined, ReloadOutlined, PlusOutlined, 
  DeleteOutlined, DownloadOutlined, UploadOutlined 
} from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';
import { listUser, addUser, updateUser, delUser, exportUser, importUser } from '@/api/user';

const loading = ref(false);
const userList = ref([]);
const selectedRowKeys = ref([]);
const open = ref(false);
const title = ref('');
const submitLoading = ref(false);
const formRef = ref();

const queryParams = reactive({
  username: '',
  realName: '',
  email: '',
  status: undefined,
  page: 1,
  size: 20
});

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
});

const columns = [
  { title: '用户ID', dataIndex: 'id', key: 'id' },
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '真实姓名', dataIndex: 'realName', key: 'realName' },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '邮箱', dataIndex: 'email', key: 'email' },
  { title: '状态', key: 'status' },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt' },
  { title: '操作', key: 'action', width: 150 }
];

const form = reactive({
  id: undefined,
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  status: 1
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
};

const getList = () => {
  loading.value = true;
  listUser(queryParams).then(res => {
    userList.value = res.data.records;
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
  queryParams.username = '';
  queryParams.realName = '';
  queryParams.email = '';
  queryParams.status = undefined;
  handleQuery();
};

const onSelectChange = (keys: any) => {
  selectedRowKeys.value = keys;
};

const handleTableChange = (pag: any) => {
  queryParams.page = pag.current;
  queryParams.size = pag.pageSize;
  getList();
};

const handleAdd = () => {
  Object.assign(form, { id: undefined, username: '', password: '', realName: '', phone: '', email: '', status: 1 });
  title.value = '新增用户';
  open.value = true;
};

const handleEdit = (record: any) => {
  Object.assign(form, record);
  title.value = '编辑用户';
  open.value = true;
};

const submitForm = () => {
  formRef.value.validate().then(() => {
    submitLoading.value = true;
    const api = form.id ? updateUser : addUser;
    api(form).then(() => {
      message.success(form.id ? '修改成功' : '新增成功');
      open.value = false;
      getList();
    }).finally(() => {
      submitLoading.value = false;
    });
  });
};

const handleDelete = (record: any) => {
  Modal.confirm({
    title: '确认删除?',
    content: `确认删除用户 "${record.username}" 吗?`,
    onOk: () => {
      delUser(record.id).then(() => {
        message.success('删除成功');
        getList();
      });
    }
  });
};

const handleBatchDelete = () => {
  const ids = selectedRowKeys.value.join(',');
  Modal.confirm({
    title: '确认批量删除?',
    content: `确认删除选中的 ${selectedRowKeys.value.length} 个用户吗?`,
    onOk: () => {
      delUser(ids).then(() => {
        message.success('批量删除成功');
        selectedRowKeys.value = [];
        getList();
      });
    }
  });
};

const handleStatusChange = (record: any, val: boolean) => {
  const newStatus = val ? 1 : 0;
  updateUser({ id: record.id, status: newStatus }).then(() => {
    record.status = newStatus;
    message.success('状态更新成功');
  });
};

const handleExport = () => {
  exportUser(queryParams).then(res => {
    const blob = new Blob([res as any], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = `用户数据_${new Date().getTime()}.xlsx`;
    link.click();
  });
};

const handleImport = (options: any) => {
  const { file, onSuccess, onError } = options;
  const formData = new FormData();
  formData.append('file', file);
  importUser(formData).then(res => {
    message.success(res.data);
    onSuccess(res.data);
    getList();
  }).catch(err => {
    onError(err);
  });
};

onMounted(() => {
  getList();
});
</script>
