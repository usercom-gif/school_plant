<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider v-model:collapsed="collapsed" collapsible>
      <div class="logo" />
      <a-menu v-model:selectedKeys="selectedKeys" theme="dark" mode="inline">
        <template v-for="item in routes" :key="item.path">
          <template v-if="!item.hidden">
            <!-- Single Child: Render as top-level item -->
            <a-menu-item
              v-if="item.children && item.children.length === 1"
              :key="resolvePath(item.path, item.children[0].path)"
              @click="navigateTo(resolvePath(item.path, item.children[0].path))"
            >
              <component :is="item.children[0].meta?.icon" />
              <span>{{ item.children[0].meta?.title }}</span>
            </a-menu-item>

            <!-- Multiple Children: Render as SubMenu -->
            <a-sub-menu
              v-else-if="item.children && item.children.length > 1"
              :key="item.path"
            >
              <template #title>
                <span>
                  <component :is="item.meta?.icon" />
                  <span>{{ item.meta?.title }}</span>
                </span>
              </template>
              <a-menu-item
                v-for="child in item.children"
                :key="resolvePath(item.path, child.path)"
                @click="navigateTo(resolvePath(item.path, child.path))"
              >
                <component :is="child.meta?.icon" />
                <span>{{ child.meta?.title }}</span>
              </a-menu-item>
            </a-sub-menu>

            <!-- No Children: Render as item (fallback) -->
            <a-menu-item
              v-else
              :key="item.path + '_fallback'"
              @click="navigateTo(item.path)"
            >
              <component :is="item.meta?.icon" />
              <span>{{ item.meta?.title }}</span>
            </a-menu-item>
          </template>
        </template>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header
        style="
          background: #fff;
          padding: 0 16px;
          display: flex;
          justify-content: space-between;
          align-items: center;
        "
      >
        <div
          class="header-left"
          style="display: flex; align-items: center; gap: 16px"
        >
          <!-- User Info on the Left -->
          <a-avatar :src="userStore.avatar || defaultAvatar" :size="32" />
          <span>您好！ {{ userStore.name }}</span>
        </div>
        <div
          class="header-right"
          style="display: flex; align-items: center; gap: 16px"
        >
          <a-button type="primary" danger @click="handleLogout"
            >退出登录</a-button
          >
        </div>
      </a-layout-header>
      <a-layout-content style="margin: 16px">
        <div
          :style="{ padding: '24px', background: '#fff', minHeight: '360px' }"
        >
          <router-view />
        </div>
      </a-layout-content>
      <a-layout-footer style="text-align: center">
        School Plant System ©2026 Created by Trae
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/store/user";
import {
  UserOutlined,
  VideoCameraOutlined,
  UploadOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
} from "@ant-design/icons-vue";

const defaultAvatar =
  "https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png";
const collapsed = ref(false);
const selectedKeys = ref<string[]>([]);
const openKeys = ref<string[]>([]); // Add openKeys for submenus
const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const routes = computed(() => userStore.routes);

// Watch route to update selected keys
watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath];

    // Auto open submenu
    const parts = newPath.split("/");
    if (parts.length > 2) {
      routes.value.forEach((item: any) => {
        if (item.path !== "/" && newPath.startsWith(item.path)) {
          if (!openKeys.value.includes(item.path)) {
            openKeys.value.push(item.path);
          }
        }
      });
    }
  },
  { immediate: true },
);

const navigateTo = (path: string) => {
  router.push(path);
};

const resolvePath = (basePath: string, routePath: string) => {
  if (routePath.startsWith("/")) {
    return routePath;
  }
  return basePath === "/" ? "/" + routePath : basePath + "/" + routePath;
};

const handleLogout = async () => {
  await userStore.logout();
  router.push("/login");
};
</script>

<style scoped>
.logo {
  height: 32px;
  margin: 16px;
  background: rgba(255, 255, 255, 0.3);
}
</style>
