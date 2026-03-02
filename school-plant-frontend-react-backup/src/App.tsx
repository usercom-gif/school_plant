import React, { lazy, Suspense } from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import AdminDashboard from "./pages/AdminDashboard";
import MaintainerTask from "./pages/MaintainerTask";
import UserCenterLayout from "./layouts/UserCenterLayout";

// Lazy load User Center pages
const UserOverview = lazy(() => import("./pages/UserCenter/Overview"));
const UserProfile = lazy(() => import("./pages/UserCenter/Profile"));
const UserSecurity = lazy(() => import("./pages/UserCenter/Security"));
const MyAdoptions = lazy(
  () => import("./pages/UserCenter/Student/MyAdoptions"),
);
const MyTasks = lazy(() => import("./pages/UserCenter/Student/MyTasks"));
const MyAchievements = lazy(
  () => import("./pages/UserCenter/Student/MyAchievements"),
);
const AdminLogs = lazy(() => import("./pages/UserCenter/Admin/AdminLogs"));
const MaintainerRecords = lazy(
  () => import("./pages/UserCenter/Maintainer/MaintainerRecords"),
);

// Placeholder components for unimplemented pages
const Placeholder = ({ title }: { title: string }) => (
  <div className="p-10 text-center text-xl text-gray-500">
    🚧 {title} 页面开发中...
  </div>
);

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />

        {/* Legacy Standalone Routes (Keep for backward compatibility if needed) */}
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/maintainer" element={<MaintainerTask />} />

        {/* New User Center Routes */}
        <Route path="/user" element={<UserCenterLayout />}>
          <Route path="overview" element={<UserOverview />} />
          <Route path="profile" element={<UserProfile />} />
          <Route path="security" element={<UserSecurity />} />
          {/* Student Routes */}
          <Route path="my-adoptions" element={<MyAdoptions />} />
          <Route path="my-tasks" element={<MyTasks />} />
          <Route path="my-abnormalities" element={<Dashboard />} />{" "}
          {/* Reuse existing */}
          <Route path="my-achievements" element={<MyAchievements />} />
          <Route
            path="my-knowledge"
            element={<Placeholder title="我的知识共享" />}
          />
          <Route
            path="my-applications"
            element={<Placeholder title="我的申请记录" />}
          />
          {/* Admin Routes */}
          <Route path="admin-logs" element={<AdminLogs />} />
          <Route
            path="admin-audits"
            element={<Placeholder title="我的审核任务" />}
          />
          <Route path="admin-abnormalities" element={<AdminDashboard />} />{" "}
          {/* Reuse existing */}
          <Route
            path="admin-evaluations"
            element={<Placeholder title="我的评比操作" />}
          />
          {/* Maintainer Routes */}
          <Route path="maintainer-pending" element={<MaintainerTask />} />{" "}
          {/* Reuse existing */}
          <Route path="maintainer-records" element={<MaintainerRecords />} />
          <Route
            path="maintainer-tracking"
            element={<Placeholder title="我的养护跟踪" />}
          />
        </Route>

        <Route path="/" element={<Navigate to="/login" replace />} />
      </Routes>
    </Router>
  );
}
