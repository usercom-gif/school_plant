import request from "@/utils/request";

export interface UserProfile {
  account: string;
  name: string;
  idNumber: string;
  phone: string;
  email?: string;
  role: string;
  registerTime: string;
  avatarUrl?: string;
  statisticNum: number;
}

export interface UpdateProfileParams {
  realName?: string;
  phone?: string;
  email?: string;
  avatarUrl?: string;
}

export interface UpdatePasswordParams {
  oldPassword?: string;
  newPassword?: string;
  confirmPassword?: string;
}

export function getUserProfile() {
  return request<any, UserProfile>({
    url: "/user/profile",
    method: "get",
  });
}

export function updateUserProfile(data: UpdateProfileParams) {
  return request<any, void>({
    url: "/user/profile",
    method: "put",
    data,
  });
}

export function updateUserPassword(data: UpdatePasswordParams) {
  return request<any, void>({
    url: "/user/password",
    method: "put",
    data,
  });
}
