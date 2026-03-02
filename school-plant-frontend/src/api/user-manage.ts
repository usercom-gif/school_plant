import request from "@/utils/request";

export interface UserVO {
  id: number;
  username: string;
  realName: string;
  phone?: string;
  email?: string;
  status: number; // 1: Active, 0: Disabled
  roleName?: string;
  roleKey?: string; // Added for frontend permission check
  createdAt?: string;
  roleId?: number; // Added for frontend logic
}

export interface UserQueryRequest {
  page?: number;
  size?: number;
  username?: string;
  realName?: string;
  email?: string;
  status?: number;
  startTime?: string;
  endTime?: string;
}

export interface UserAddRequest {
  username: string;
  password?: string; // Optional if backend sets default
  realName: string;
  phone?: string;
  email?: string;
  roleId?: number;
  status?: number;
}

export interface UserUpdateRequest {
  id: number;
  phone?: string;
  email?: string;
  roleId?: number;
  status?: number;
}

export interface RoleVO {
  id: number;
  roleName: string;
  roleKey: string;
  status: number;
}

// User APIs
export function getUserList(params: UserQueryRequest) {
  return request<any>({
    url: "/system/user/list",
    method: "get",
    params,
  });
}

export function addUser(data: UserAddRequest) {
  return request({
    url: "/system/user",
    method: "post",
    data,
  });
}

export function updateUser(data: UserUpdateRequest) {
  return request({
    url: "/system/user",
    method: "put",
    data,
  });
}

export function deleteUser(ids: number[]) {
  return request({
    url: `/system/user/${ids.join(",")}`,
    method: "delete",
  });
}

// Role APIs (Simplified for Dropdown)
export function getRoleList() {
  return request<any>({
    url: "/system/role/list",
    method: "get",
    params: { page: 1, size: 100 }, // Get all roles
  });
}
