import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/Login.vue"),
  },
  {
    path: "/",
    redirect: "/login",
  },
  {
    path: "/user",
    component: () => import("@/layout/UserCenterLayout.vue"),
    redirect: "/user/overview",
    children: [
      {
        path: "overview",
        name: "UserOverview",
        component: () => import("@/views/UserCenter/Overview.vue"),
      },
      {
        path: "profile",
        name: "UserProfile",
        component: () => import("@/views/UserCenter/Profile.vue"),
      },
      // Student Routes
      {
        path: "plant-search",
        component: () => import("@/views/PlantSearch/index.vue"),
      },
      {
        path: "my-applications",
        component: () => import("@/views/Adoption/MyApplications.vue"),
      },
      {
        path: "my-tasks",
        component: () => import("@/views/TaskManage/MyTasks.vue"),
      },
      {
        path: "report-abnormality",
        component: () => import("@/views/Abnormality/Report.vue"),
      },
      {
        path: "my-abnormalities",
        component: () => import("@/views/Abnormality/Manage.vue"), // Reusing Manage for user history
      },
      {
        path: "my-achievements",
        component: () => import("@/views/Placeholder.vue"),
      },
      {
        path: "knowledge-share",
        component: () => import("@/views/KnowledgeShare/index.vue"),
      },

      // Admin Routes
      {
        path: "plant-manage",
        component: () => import("@/views/PlantManage/index.vue"),
      },
      {
        path: "audit-applications",
        component: () => import("@/views/Adoption/Audit.vue"),
      },
      {
        path: "task-manage",
        component: () => import("@/views/TaskManage/index.vue"),
      },
      {
        path: "abnormality-manage",
        component: () => import("@/views/Abnormality/Manage.vue"),
      },
      {
        path: "achievement-eval",
        component: () => import("@/views/Placeholder.vue"),
      },
      {
        path: "content-audit",
        component: () => import("@/views/KnowledgeShare/Audit.vue"),
      },
      {
        path: "user-manage",
        component: () => import("@/views/UserManage/index.vue"),
      },
      {
        path: "system-params",
        component: () => import("@/views/Placeholder.vue"),
      },
      {
        path: "operation-logs",
        component: () => import("@/views/Placeholder.vue"),
      },

      // Maintainer Routes
      {
        path: "maintainer-pending",
        component: () => import("@/views/Placeholder.vue"),
      },
      {
        path: "maintainer-tracking",
        component: () => import("@/views/Placeholder.vue"),
      },
      {
        path: "maintainer-records",
        component: () => import("@/views/Placeholder.vue"),
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role") || "USER";
  console.log(
    `[Router Guard] To: ${to.path}, Token: ${!!token}, Role: ${role}`,
  );

  if (to.path !== "/login" && !token) {
    console.log("[Router Guard] Redirecting to login (no token)");
    next("/login");
  } else {
    // Basic Role Guard (Can be enhanced with more specific route meta)
    if (
      to.path.includes("/plant-manage") ||
      to.path.includes("/audit-applications") ||
      to.path.includes("/task-manage") ||
      to.path.includes("/user-manage")
    ) {
      if (role !== "ADMIN") {
        console.warn("[Router Guard] Unauthorized access to Admin area");
        next("/user/overview"); // Redirect unauthorized access to overview
        return;
      }
    }
    if (to.path.includes("/maintainer-pending")) {
      if (role !== "MAINTAINER" && role !== "ADMIN") {
        console.warn("[Router Guard] Unauthorized access to Maintainer area");
        next("/user/overview");
        return;
      }
    }
    next();
  }
});

export default router;
