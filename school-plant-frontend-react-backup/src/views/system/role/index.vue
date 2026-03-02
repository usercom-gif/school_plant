<template>
  <div class="app-container" style="padding: 20px">
    <a-card :bordered="false">
      <!-- Search Form -->
      <a-form layout="inline" style="margin-bottom: 20px">
        <a-form-item label="角色名称">
          <a-input
            v-model:value="queryParams.roleName"
            placeholder="请输入角色名称"
            allowClear
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="queryParams.status"
            placeholder="角色状态"
            style="width: 120px"
            allowClear
          >
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
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
        <a-button
          type="primary"
          ghost
          style="margin-left: 8px"
          @click="handleExport"
        >
          <template #icon><DownloadOutlined /></template>导出
        </a-button>
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
        :data-source="roleList"
        :row-selection="{
          selectedRowKeys: selectedRowKeys,
          onChange: onSelectChange,
        }"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-switch
              :checked="record.status === 1"
              @change="(val: boolean) => handleStatusChange(record, val)"
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
      <a-form
        :model="form"
        :rules="rules"
        ref="formRef"
        :label-col="{ span: 6 }"
      >
        <a-form-item label="角色名称" name="roleName">
          <a-input v-model:value="form.roleName" placeholder="请输入角色名称" />
        </a-form-item>
        <a-form-item label="角色标识" name="roleKey">
          <a-input
            v-model:value="form.roleKey"
            placeholder="请输入角色标识"
            :disabled="!!form.id && form.isSystemRole === 1"
          />
        </a-form-item>
        <a-form-item label="角色描述" name="description">
          <a-textarea
            v-model:value="form.description"
            placeholder="请输入角色描述"
          />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">正常</a-radio>
            <a-radio :value="0">停用</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="菜单权限">
          <a-tree
            v-model:checkedKeys="form.permissionIds"
            checkable
            :tree-data="permissionOptions"
            :field-names="{ children: 'children', title: 'label', key: 'id' }"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined,
  DownloadOutlined,
} from "@ant-design/icons-vue";
import { message, Modal } from "ant-design-vue";
import {
  listRole,
  getRole,
  addRole,
  updateRole,
  delRole,
  changeRoleStatus,
  exportRole,
} from "@/api/role";

const loading = ref(false);
const roleList = ref([]);
const selectedRowKeys = ref<number[]>([]);
const open = ref(false);
const title = ref("");
const submitLoading = ref(false);
const formRef = ref();

// 模拟权限数据，实际应从后端获取菜单树
const permissionOptions = ref([
  {
    id: 1,
    label: "系统管理",
    children: [
      { id: 101, label: "用户管理" },
      { id: 102, label: "角色管理" },
      { id: 103, label: "菜单管理" },
      { id: 104, label: "部门管理" },
    ],
  },
  {
    id: 2,
    label: "植物管理",
    children: [
      { id: 201, label: "植物列表" },
      { id: 202, label: "养护任务" },
    ],
  },
]);

const queryParams = reactive({
  roleName: "",
  status: undefined,
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
  { title: "角色编号", dataIndex: "id", key: "id" },
  { title: "角色名称", dataIndex: "roleName", key: "roleName" },
  { title: "角色标识", dataIndex: "roleKey", key: "roleKey" },
  { title: "描述", dataIndex: "description", key: "description" },
  { title: "状态", key: "status" },
  { title: "创建时间", dataIndex: "createdAt", key: "createdAt" },
  { title: "操作", key: "action", width: 150 },
];

interface RoleForm {
  id?: number;
  roleName: string;
  roleKey: string;
  description: string;
  status: number;
  isSystemRole?: number;
  permissionIds: number[];
}

const form = reactive<RoleForm>({
  id: undefined,
  roleName: "",
  roleKey: "",
  description: "",
  status: 1,
  permissionIds: [],
});

const rules = {
  roleName: [{ required: true, message: "请输入角色名称", trigger: "blur" }],
  roleKey: [{ required: true, message: "请输入角色标识", trigger: "blur" }],
};

const getList = () => {
  loading.value = true;
  listRole(queryParams)
    .then((res) => {
      roleList.value = res.data.records;
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
  queryParams.roleName = "";
  queryParams.status = undefined;
  handleQuery();
};

const onSelectChange = (keys: any[]) => {
  selectedRowKeys.value = keys;
};

const handleTableChange = (pag: any) => {
  queryParams.page = pag.current;
  queryParams.size = pag.pageSize;
  getList();
};

const handleAdd = () => {
  Object.assign(form, {
    id: undefined,
    roleName: "",
    roleKey: "",
    description: "",
    status: 1,
    isSystemRole: 0,
    permissionIds: [],
  });
  title.value = "新增角色";
  open.value = true;
};

const handleEdit = (record: any) => {
  // 获取详情以回显权限
  getRole(record.id).then((res) => {
    Object.assign(form, res.data);
    // permissionIds 从后端返回
    title.value = "编辑角色";
    open.value = true;
  });
};

const submitForm = () => {
  formRef.value.validate().then(() => {
    submitLoading.value = true;
    const api = form.id ? updateRole : addRole;
    api(form)
      .then(() => {
        message.success(form.id ? "修改成功" : "新增成功");
        open.value = false;
        getList();
      })
      .finally(() => {
        submitLoading.value = false;
      });
  });
};

const handleDelete = (record: any) => {
  Modal.confirm({
    title: "确认删除?",
    content: `确认删除角色 "${record.roleName}" 吗?`,
    onOk: () => {
      delRole(String(record.id)).then(() => {
        message.success("删除成功");
        getList();
      });
    },
  });
};

const handleBatchDelete = () => {
  const ids = selectedRowKeys.value.join(",");
  Modal.confirm({
    title: "确认批量删除?",
    content: `确认删除选中的 ${selectedRowKeys.value.length} 个角色吗?`,
    onOk: () => {
      delRole(ids).then(() => {
        message.success("批量删除成功");
        selectedRowKeys.value = [];
        getList();
      });
    },
  });
};

const handleStatusChange = (record: any, val: boolean) => {
  const newStatus = val ? 1 : 0;
  changeRoleStatus(record.id, newStatus)
    .then(() => {
      record.status = newStatus;
      message.success("状态更新成功");
    })
    .catch(() => {
      // Revert switch if failed (though record.status isn't reactive here usually unless using v-model)
      // Since we manually update record.status on success, UI stays consistent
    });
};

const handleExport = () => {
  exportRole(queryParams).then((res) => {
    const blob = new Blob([res as any], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });
    const link = document.createElement("a");
    link.href = window.URL.createObjectURL(blob);
    link.download = `角色数据_${new Date().getTime()}.xlsx`;
    link.click();
  });
};

onMounted(() => {
  getList();
});
</script>
