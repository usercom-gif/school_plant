<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      theme="light"
      class="shadow-md z-10"
      width="220"
    >
      <div
        class="h-16 flex items-center justify-center border-b border-gray-100"
      >
        <div
          class="font-bold text-green-600 transition-all duration-300"
          :class="collapsed ? 'text-xl' : 'text-lg'"
        >
          {{ collapsed ? "SP" : "School Plant" }}
        </div>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        theme="light"
        mode="inline"
        @click="handleMenuClick"
        :items="menuItems"
      >
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header
        style="background: #fff; padding: 0"
        class="flex justify-between items-center px-6 shadow-sm z-10"
      >
        <component
          :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined"
          class="trigger"
          @click="() => (collapsed = !collapsed)"
        />

        <div class="flex items-center gap-4">
          <span class="text-gray-500">欢迎回来, {{ username }}</span>
          <a-dropdown>
            <div
              class="flex items-center gap-2 cursor-pointer hover:bg-gray-50 px-3 py-1 rounded-full transition-colors"
            >
              <a-avatar style="background-color: #87d068">
                <template #icon><UserOutlined /></template>
              </a-avatar>
              <span class="font-medium text-gray-700">{{ username }}</span>
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item key="logout" @click="handleLogout">
                  <LogoutOutlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content
        style="
          margin: 24px 16px;
          padding: 24px;
          background: #f0f2f5;
          min-height: 280px;
        "
      >
        <router-view></router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script lang="ts" setup>
import { ref, computed, h, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  UserOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  LogoutOutlined,
} from "@ant-design/icons-vue";
import { getMenusByRole } from "@/config/menu";
import { Badge, Modal } from "ant-design-vue";
import { getPendingAuditCount } from "@/api/adoption";

const collapsed = ref(false);
const router = useRouter();
const route = useRoute();
const selectedKeys = ref([route.path]);
const openKeys = ref<string[]>([]);
const username = localStorage.getItem("username") || "用户";
const role = localStorage.getItem("role") || "USER";
const pendingAuditCount = ref(0);

// Generate menu items structure for Ant Design Vue Menu
const menuItems = computed(() => {
  const menus = getMenusByRole(role);
  return menus.map((item) => {
    if (item.type === "group") {
      return {
        key: item.key,
        label: item.label,
        type: "group",
        children: item.children?.map((child) => {
          let labelVNode: any = child.label;
          // Add badge for Audit Applications
          if (
            child.key === "/user/audit-applications" &&
            pendingAuditCount.value > 0
          ) {
            labelVNode = h(
              "div",
              { class: "flex items-center justify-between w-full pr-4" },
              [
                h("span", child.label),
                h(Badge, {
                  count: pendingAuditCount.value,
                  numberStyle: { backgroundColor: "#ff4d4f" },
                }),
              ],
            );
          }
          return {
            key: child.key,
            label: labelVNode,
            // Directly pass the icon component if available, fallback to function if complex
            icon: child.icon ? h(child.icon) : undefined,
          };
        }),
      };
    }
    return {
      key: item.key,
      label: item.label,
      icon: item.icon ? h(item.icon) : undefined,
      children: item.children,
    };
  });
});

const fetchPendingCount = async () => {
  if (role === "ADMIN") {
    try {
      const res: any = await getPendingAuditCount();
      pendingAuditCount.value = res;
    } catch (e) {
      console.error("Failed to fetch pending audit count", e);
    }
  }
};

const handleMenuClick = ({ key }: { key: string }) => {
  router.push(key);
};

const handleLogout = () => {
  Modal.confirm({
    title: "确认退出",
    content: "确定要退出登录吗？",
    okText: "确认",
    cancelText: "取消",
    onOk() {
      localStorage.clear();
      router.push("/login");
    },
  });
};

onMounted(() => {
  fetchPendingCount();
});
</script>

<style scoped>
.trigger {
  font-size: 18px;
  line-height: 64px;
  padding: 0 24px;
  cursor: pointer;
  transition: color 0.3s;
}
.trigger:hover {
  color: #1890ff;
}
</style>
