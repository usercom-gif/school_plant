import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import { useUserStore } from "@/store/user";
import { message } from "ant-design-vue";

// 基础路由
const routes: RouteRecordRaw[] = [
  {
    path: "/login",
    component: () => import("@/views/login/index.vue"),
    meta: { title: "登录" },
  },
  {
    path: "/403",
    component: () => import("@/views/error/403.vue"),
    meta: { title: "无权限" },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  const token = userStore.token;

  // 1. 无Token -> 登录页
  if (!token) {
    if (to.path === "/login") return next();
    return next("/login");
  }

  // 2. 有Token -> 登录页 -> 跳转首页
  if (to.path === "/login") {
    return next("/");
  }

  // 3. 已获取用户信息 -> 放行
  if (userStore.roles.length > 0) {
    // 如果访问根路径 /，重定向到第一个可用菜单
    if (to.path === "/") {
      const firstRoute = userStore.routes[0];
      if (firstRoute) {
        if (firstRoute.children && firstRoute.children.length > 0) {
          // 如果第一个路由有子路由，跳转到第一个子路由
          return next(firstRoute.path + "/" + firstRoute.children[0].path);
        }
        return next(firstRoute.path);
      }
    }
    return next();
  }

  // 4. 未获取用户信息 -> 拉取信息 + 生成动态路由
  try {
    // 拉取用户信息
    await userStore.getUserInfo();
    // 生成动态路由
    const accessRoutes = await userStore.generateRoutes();

    // 动态添加路由
    accessRoutes.forEach((route: any) => {
      router.addRoute(route);
    });

    // 重要：确保404路由最后添加，否则会拦截所有动态路由
    router.addRoute({
      path: "/:pathMatch(.*)*",
      component: () => import("@/views/error/404.vue"),
      meta: { title: "页面不存在" },
    });

    // Hack: 确保addRoute完成后，重新进入当前路由
    // 如果是 /，则重定向到第一个路由
    if (to.path === "/") {
      const firstRoute = accessRoutes[0];
      if (firstRoute) {
        if (firstRoute.children && firstRoute.children.length > 0) {
          return next({
            path: firstRoute.path + "/" + firstRoute.children[0].path,
            replace: true,
          });
        }
        return next({ path: firstRoute.path, replace: true });
      }
    }
    return next({ ...to, replace: true });
  } catch (error) {
    // 拉取失败 -> 清除Token -> 登录页
    await userStore.resetToken();
    message.error("身份验证失败，请重新登录");
    next("/login");
  }
});

export default router;
