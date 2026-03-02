import { defineStore } from "pinia";
import { login, getUserInfo, logout } from "@/api/user";
import Layout from "@/layout/index.vue";

// 静态路由表配置
const staticRoutes = {
  USER: [
    {
      path: "/dashboard",
      component: "Layout",
      children: [
        {
          path: "",
          component: "dashboard/index",
          meta: { title: "首页", icon: "HomeOutlined", affix: true },
        },
      ],
    },
    {
      path: "/profile",
      component: "Layout",
      children: [
        {
          path: "",
          component: "system/user/profile",
          meta: { title: "个人中心", icon: "UserOutlined" },
        },
      ],
    },
    {
      path: "/plant",
      component: "Layout",
      children: [
        {
          path: "query",
          component: "plant/query/index",
          meta: { title: "植物查询", icon: "SearchOutlined" },
        },
      ],
    },
    {
      path: "/adoption",
      component: "Layout",
      children: [
        {
          path: "apply",
          component: "adoption/apply",
          meta: { title: "认养申请", icon: "FormOutlined" },
        },
      ],
    },
    {
      path: "/task",
      component: "Layout",
      children: [
        {
          path: "handle",
          component: "task/handle",
          meta: { title: "任务处理", icon: "CheckSquareOutlined" },
        },
      ],
    },
    {
      path: "/abnormality",
      component: "Layout",
      children: [
        {
          path: "report",
          component: "abnormality/report",
          meta: { title: "异常上报", icon: "WarningOutlined" },
        },
      ],
    },
    {
      path: "/achievement",
      component: "Layout",
      children: [
        {
          path: "query",
          component: "achievement/query",
          meta: { title: "成果查询", icon: "TrophyOutlined" },
        },
      ],
    },
    {
      path: "/knowledge",
      component: "Layout",
      children: [
        {
          path: "",
          component: "knowledge/index",
          meta: { title: "知识共享", icon: "ReadOutlined" },
        },
      ],
    },
  ],
  ADMIN: [
    {
      path: "/dashboard",
      component: "Layout",
      children: [
        {
          path: "",
          component: "dashboard/index",
          meta: { title: "首页", icon: "HomeOutlined", affix: true },
        },
      ],
    },
    {
      path: "/system",
      component: "Layout",
      name: "System",
      meta: { title: "系统管理", icon: "SettingOutlined" },
      redirect: "/system/user",
      children: [
        {
          path: "user",
          component: "system/user/index",
          name: "UserManage",
          meta: { title: "用户管理", icon: "UserOutlined" },
        },
        {
          path: "approval",
          component: "system/approval/index",
          name: "ApprovalManage",
          meta: { title: "更名审批", icon: "AuditOutlined" },
        },
        {
          path: "role",
          component: "system/role/index",
          name: "RoleManage",
          meta: { title: "角色管理", icon: "TeamOutlined" },
        },
        {
          path: "menu",
          component: "system/menu/index",
          name: "MenuManage",
          meta: { title: "菜单管理", icon: "MenuOutlined" },
        },
        {
          path: "config",
          component: "system/config/index",
          name: "SysConfig",
          meta: { title: "系统参数", icon: "ToolOutlined" },
        },
        {
          path: "log",
          component: "system/log/index",
          name: "OpLog",
          meta: { title: "操作日志", icon: "FileTextOutlined" },
        },
        {
          path: "dict",
          component: "system/dict/index",
          name: "DictManage",
          meta: { title: "字典管理", icon: "BookOutlined" },
        },
      ],
    },
    {
      path: "/plant",
      component: "Layout",
      meta: { title: "植物管理", icon: "AppstoreOutlined" },
      children: [
        {
          path: "query",
          component: "plant/query/index",
          meta: { title: "植物查询", icon: "SearchOutlined" },
        },
        {
          path: "admin",
          component: "system/plant/index",
          meta: { title: "植物管理", icon: "AppstoreOutlined" },
        },
      ],
    },
    {
      path: "/adoption",
      component: "Layout",
      children: [
        {
          path: "audit",
          component: "adoption/audit/index", // Updated to index
          meta: { title: "认养审核", icon: "AuditOutlined" },
        },
      ],
    },
    {
      path: "/task",
      component: "Layout",
      children: [
        {
          path: "template",
          component: "system/task-template/index",
          meta: { title: "任务模板管理", icon: "ScheduleOutlined" },
        },
      ],
    },
    {
      path: "/abnormality",
      component: "Layout",
      children: [
        {
          path: "dispatch",
          component: "abnormality/dispatch",
          meta: { title: "异常分派", icon: "SendOutlined" },
        },
      ],
    },
    {
      path: "/achievement",
      component: "Layout",
      children: [
        {
          path: "review",
          component: "achievement/review",
          meta: { title: "成果评比", icon: "StarOutlined" },
        },
      ],
    },
    {
      path: "/knowledge",
      component: "Layout",
      children: [
        {
          path: "audit",
          component: "knowledge/audit",
          meta: { title: "知识审核", icon: "SafetyCertificateOutlined" },
        },
      ],
    },
    // USER functions for ADMIN
    {
      path: "/profile",
      component: "Layout",
      children: [
        {
          path: "",
          component: "system/user/profile",
          meta: { title: "个人中心", icon: "UserOutlined" },
        },
      ],
    },
  ],
  MAINTAINER: [
    {
      path: "/dashboard",
      component: "Layout",
      children: [
        {
          path: "",
          component: "dashboard/index",
          meta: { title: "首页", icon: "HomeOutlined", affix: true },
        },
      ],
    },
    {
      path: "/abnormality",
      component: "Layout",
      children: [
        {
          path: "handle",
          component: "abnormality/handle",
          meta: { title: "异常处理", icon: "MedicineBoxOutlined" },
        },
      ],
    },
    {
      path: "/task",
      component: "Layout",
      children: [
        {
          path: "track",
          component: "task/track",
          meta: { title: "任务跟踪", icon: "EyeOutlined" },
        },
      ],
    },
    {
      path: "/personal",
      component: "Layout",
      children: [
        {
          path: "record",
          component: "personal/record",
          meta: { title: "处理记录", icon: "HistoryOutlined" },
        },
      ],
    },
    {
      path: "/profile",
      component: "Layout",
      children: [
        {
          path: "",
          component: "system/user/profile",
          meta: { title: "个人中心", icon: "UserOutlined" },
        },
      ],
    },
  ],
};

export const useUserStore = defineStore("user", {
  state: () => ({
    token: localStorage.getItem("token") || "",
    name: "",
    avatar: "",
    roles: [] as string[],
    permissions: [] as string[],
    routes: [] as any[], // 动态路由表
    userId: 0,
  }),
  actions: {
    // 登录
    async login(userInfo: any) {
      const res = await login(userInfo);
      this.token = res.data.tokenInfo.tokenValue;
      localStorage.setItem("token", this.token);
      return res;
    },

    // 获取用户信息
    async getUserInfo() {
      const res = await getUserInfo();
      this.userId = res.data.user.id;
      this.name = res.data.user.realName;
      this.avatar = res.data.user.avatarUrl; // Set avatar from API
      this.roles = [res.data.role.roleKey]; // USER, ADMIN, MAINTAINER
      this.permissions = res.data.permissions;
      return res;
    },

    // 生成动态路由
    async generateRoutes() {
      // 获取用户角色
      const roleKey = this.roles[0] || "USER";

      // 根据角色获取静态路由配置
      let roleRoutes = (staticRoutes as any)[roleKey] || staticRoutes["USER"];

      // 深拷贝以防止修改原配置
      const sdata = JSON.parse(JSON.stringify(roleRoutes));
      const rdata = JSON.parse(JSON.stringify(roleRoutes));

      const sidebarRoutes = filterAsyncRouter(sdata);
      const rewriteRoutes = filterAsyncRouter(rdata, false, true);

      this.routes = sidebarRoutes;
      return rewriteRoutes;
    },

    // 退出
    async logout() {
      // 1. 调用后端退出
      try {
        await logout();
      } catch (e) {
        console.warn("Logout API failed:", e);
      }

      // 2. 清除前端状态
      this.token = "";
      this.roles = [];
      this.permissions = [];
      localStorage.removeItem("token");
    },

    async resetToken() {
      this.token = "";
      this.roles = [];
      this.permissions = [];
      localStorage.removeItem("token");
    },
  },
});

// 遍历后台传来的路由字符串，转换为组件对象
function filterAsyncRouter(
  asyncRouterMap: any[],
  lastRouter: any = false,
  type = false,
): any[] {
  return asyncRouterMap.filter((route: any) => {
    if (type && route.children) {
      route.children = filterChildren(route.children);
    }
    if (route.component) {
      // Layout组件特殊处理
      if (route.component === "Layout") {
        route.component = Layout;
      } else {
        route.component = loadView(route.component);
      }
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type);
    } else {
      delete route["children"];
      delete route["redirect"];
    }
    return true;
  });
}

function filterChildren(childrenMap: any[], lastRouter: any = false): any[] {
  var children: any[] = [];
  childrenMap.forEach((el: any, index: any) => {
    if (el.children && el.children.length) {
      if (el.component === "ParentView" && !lastRouter) {
        el.children.forEach((c: any) => {
          c.path = el.path + "/" + c.path;
          if (c.children && c.children.length) {
            children = children.concat(filterChildren(c.children, c));
            return;
          }
          children.push(c);
        });
        return;
      }
    }
    if (lastRouter) {
      el.path = lastRouter.path + "/" + el.path;
    }
    children = children.concat(el);
  });
  return children;
}

const modules = import.meta.glob("../views/**/*.vue");
export const loadView = (view: any) => {
  let res;
  for (const path in modules) {
    const dir = path.split("views/")[1].split(".vue")[0];
    if (dir === view) {
      res = () => modules[path]();
    }
  }
  return res;
};
