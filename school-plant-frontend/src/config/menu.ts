import {
  UserOutlined,
  HomeOutlined,
  AppstoreOutlined,
  FormOutlined,
  CalendarOutlined,
  WarningOutlined,
  TrophyOutlined,
  ShareAltOutlined,
  SettingOutlined,
  FileTextOutlined,
  CheckSquareOutlined,
  AuditOutlined,
  BarChartOutlined,
  SearchOutlined,
} from "@ant-design/icons-vue";

export interface MenuItem {
  key: string;
  icon?: any;
  label: string;
  children?: MenuItem[];
  type?: "group";
}

export const getMenusByRole = (role: string): MenuItem[] => {
  const commonMenus: MenuItem[] = [
    {
      key: "/user/overview",
      icon: HomeOutlined,
      label: "个人中心首页",
    },
    {
      key: "/user/profile",
      icon: UserOutlined,
      label: "基础信息管理",
    },
  ];

  let roleMenus: MenuItem[] = [];

  if (role === "USER") {
    roleMenus = [
      {
        key: "plant-service",
        label: "植物认养服务",
        type: "group",
        children: [
          {
            key: "/user/plant-search",
            icon: SearchOutlined,
            label: "植物信息查询",
          },
          {
            key: "/user/my-applications",
            icon: FormOutlined,
            label: "认养申请",
          },
          {
            key: "/user/my-tasks",
            icon: CalendarOutlined,
            label: "养护任务处理",
          },
          {
            key: "/user/my-abnormalities",
            icon: WarningOutlined,
            label: "植物异常上报",
          },
        ],
      },
      {
        key: "community",
        label: "社区与成果",
        type: "group",
        children: [
          {
            key: "/user/my-achievements",
            icon: TrophyOutlined,
            label: "个人成果查询",
          },
          {
            key: "/user/knowledge-share",
            icon: ShareAltOutlined,
            label: "知识共享社区",
          },
        ],
      },
    ];
  } else if (role === "ADMIN") {
    roleMenus = [
      {
        key: "manage-core",
        label: "核心管理",
        type: "group",
        children: [
          {
            key: "/user/plant-manage",
            icon: AppstoreOutlined,
            label: "植物信息管理",
          },
          {
            key: "/user/audit-applications",
            icon: CheckSquareOutlined,
            label: "认养申请审核",
          },
          {
            key: "/user/task-manage",
            icon: CalendarOutlined,
            label: "养护任务管理",
          },
          {
            key: "/user/abnormality-dispatch",
            icon: WarningOutlined,
            label: "异常上报分派",
          },
        ],
      },
      {
        key: "manage-community",
        label: "社区与运营",
        type: "group",
        children: [
          {
            key: "/user/achievement-eval",
            icon: TrophyOutlined,
            label: "成果评比",
          },
          {
            key: "/user/content-audit",
            icon: AuditOutlined,
            label: "知识共享审核",
          },
        ],
      },
      {
        key: "system",
        label: "系统管理",
        type: "group",
        children: [
          {
            key: "/user/user-manage",
            icon: UserOutlined,
            label: "用户管理",
          },
          {
            key: "/user/system-params",
            icon: SettingOutlined,
            label: "系统参数配置",
          },
          {
            key: "/user/operation-logs",
            icon: FileTextOutlined,
            label: "操作日志查询",
          },
        ],
      },
    ];
  } else if (role === "MAINTAINER") {
    roleMenus = [
      {
        key: "maintainer-work",
        label: "养护工作台",
        type: "group",
        children: [
          {
            key: "/user/maintainer-pending",
            icon: WarningOutlined,
            label: "异常上报处理",
          },
          {
            key: "/user/maintainer-tracking",
            icon: AppstoreOutlined,
            label: "养护任务跟踪",
          },
          {
            key: "/user/maintainer-records",
            icon: BarChartOutlined,
            label: "个人处理记录",
          },
        ],
      },
    ];
  }

  return [...commonMenus, ...roleMenus];
};
