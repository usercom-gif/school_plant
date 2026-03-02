import request from "@/utils/request";

export interface UserProfile {
  account: string;
  name: string;
  idNumber: string;
  phone: string;
  role: string;
  registerTime: string;
  avatarUrl?: string;
  statisticNum: number;
}

export function getUserProfile() {
  return request<UserProfile>({
    url: "/user/profile",
    method: "get",
  });
}
