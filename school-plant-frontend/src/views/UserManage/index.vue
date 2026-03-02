<template>
  <div>
    <!-- Search Bar -->
    <a-card class="mb-4 shadow-sm" :bordered="false">
      <a-form layout="inline" :model="queryParams" @finish="handleSearch">
        <a-form-item label="用户名">
          <a-input v-model:value="queryParams.username" placeholder="请输入用户名" allow-clear />
        </a-form-item>
        <a-form-item label="真实姓名">
          <a-input v-model:value="queryParams.realName" placeholder="请输入真实姓名" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryParams.status" placeholder="状态" style="width: 120px" allow-clear>
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading">查询</a-button>
          <a-button class="ml-2" @click="resetQuery">重置</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- Table Area -->
    <a-card class="shadow-sm" :bordered="false">
      <div class="mb-4 flex justify-between">
        <div class="text-lg font-bold">用户列表</div>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增用户
        </a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '正常' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a :disabled="record.roleKey === 'ADMIN' && record.username !== currentUsername" @click="handleEdit(record)">编辑</a>
              <a-popconfirm title="确定要删除吗?" @confirm="handleDelete(record.id)" :disabled="record.roleKey === 'ADMIN'">
                <a :class="{'text-red-500': record.roleKey !== 'ADMIN', 'text-gray-400': record.roleKey === 'ADMIN'}" :disabled="record.roleKey === 'ADMIN'">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Add/Edit Modal -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      @ok="handleModalOk"
      :confirmLoading="modalLoading"
    >
      <a-form
        ref="modalFormRef"
        :model="formState"
        layout="vertical"
        :rules="rules"
      >
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="formState.username" :disabled="!!formState.id" />
        </a-form-item>
        <a-form-item label="真实姓名" name="realName">
          <a-input v-model:value="formState.realName" />
        </a-form-item>
        <a-form-item label="密码" name="password" v-if="!formState.id">
          <a-input-password v-model:value="formState.password" placeholder="默认 123456" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="formState.phone" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="formState.email" />
        </a-form-item>
        <a-form-item label="角色" name="roleId">
          <a-select v-model:value="formState.roleId" placeholder="请选择角色">
            <a-select-option v-for="role in roleList" :key="role.id" :value="role.id">
              {{ role.roleName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formState.status">
            <a-radio :value="1">正常</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { getUserList, addUser, updateUser, deleteUser, getRoleList, type UserVO, type UserQueryRequest, type RoleVO } from '@/api/user-manage';

// --- State ---
const loading = ref(false);
const dataSource = ref<UserVO[]>([]);
const roleList = ref<RoleVO[]>([]);
const currentUsername = localStorage.getItem('username'); // Get current logged-in user
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
});

const queryParams = reactive<UserQueryRequest>({
  username: '',
  realName: '',
  status: undefined,
});

// --- Modal State ---
const modalVisible = ref(false);
const modalTitle = ref('新增用户');
const modalLoading = ref(false);
const modalFormRef = ref();
const formState = reactive<any>({
  id: undefined,
  username: '',
  realName: '',
  password: '',
  phone: '',
  email: '',
  roleId: undefined,
  status: 1,
});

const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  realName: [{ required: true, message: '请输入真实姓名' }],
  roleId: [{ required: true, message: '请选择角色' }],
};

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '用户名', dataIndex: 'username' },
  { title: '真实姓名', dataIndex: 'realName' },
  { title: '手机号', dataIndex: 'phone' },
  { title: '邮箱', dataIndex: 'email' },
  { title: '状态', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt' },
  { title: '操作', key: 'action', width: 150 },
];

// --- Methods ---

const fetchData = async () => {
  loading.value = true;
  try {
    const res: any = await getUserList({
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

const fetchRoles = async () => {
  try {
    const res: any = await getRoleList();
    roleList.value = res.records || [];
  } catch (e) {}
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const resetQuery = () => {
  queryParams.username = '';
  queryParams.realName = '';
  queryParams.status = undefined;
  handleSearch();
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchData();
};

const handleAdd = () => {
  modalTitle.value = '新增用户';
  formState.id = undefined;
  formState.username = '';
  formState.realName = '';
  formState.password = '123456';
  formState.phone = '';
  formState.email = '';
  formState.roleId = undefined;
  formState.status = 1;
  modalVisible.value = true;
};

const handleEdit = (record: UserVO) => {
  modalTitle.value = '编辑用户';
  Object.assign(formState, record);
  // Password not needed for edit
  formState.password = undefined; 
  modalVisible.value = true;
};

const handleDelete = async (id: number) => {
  try {
    await deleteUser([id]);
    message.success('删除成功');
    fetchData();
  } catch (error) {}
};

const handleModalOk = async () => {
  try {
    await modalFormRef.value.validate();
    modalLoading.value = true;
    if (formState.id) {
      await updateUser(formState);
      message.success('更新成功');
    } else {
      await addUser(formState);
      message.success('新增成功');
    }
    modalVisible.value = false;
    fetchData();
  } catch (error) {
    console.error(error);
  } finally {
    modalLoading.value = false;
  }
};

onMounted(() => {
  fetchData();
  fetchRoles();
});
</script>
