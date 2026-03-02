import request from "@/utils/request";

export interface AdoptionApplication {
  id: number;
  userId: number;
  plantId: number;
  userName?: string;
  plantName?: string;
  careExperience: string; // Renamed from reason to match backend
  adoptionPeriodMonths?: number;
  status: string; // 'PENDING', 'APPROVED', 'REJECTED'
  rejectionReason?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AdoptionAuditLog {
  id: number;
  applicationId: number;
  operatorId: number;
  operatorName?: string;
  action: string; // 'APPROVE', 'REJECT'
  comment: string;
  createdAt: string;
}

export interface AdoptionQueryRequest {
  page?: number;
  size?: number;
  status?: string;
  userId?: number;
}

export interface AdoptionApplyRequest {
  plantId: number;
  adoptionPeriodMonths: number;
  careExperience: string;
  contactPhone: string;
}

export interface AuditApplicationRequest {
  id: number;
  action: "PASS" | "REJECT";
  comment?: string;
}

// User APIs
export function getMyApplications(params: { page?: number; size?: number }) {
  return request<any, any>({
    url: "/adoption/my-applications",
    method: "get",
    params,
  });
}

export function submitApplication(data: AdoptionApplyRequest) {
  return request<any, void>({
    url: "/adoption/submit",
    method: "post",
    data,
  });
}

export function getAuditLogs(applicationId: number) {
  return request<any, AdoptionAuditLog[]>({
    url: `/adoption/audit/logs/${applicationId}`,
    method: "get",
  });
}

// Admin APIs
export function getAuditList(params: AdoptionQueryRequest) {
  return request<any, any>({
    url: "/adoption/audit/list",
    method: "get",
    params,
  });
}

export function auditApplication(data: AuditApplicationRequest) {
  return request<any, void>({
    url: "/adoption/audit/action",
    method: "post",
    data,
  });
}

export function getPendingAuditCount() {
  return request<any, number>({
    url: "/adoption/audit/pending-count",
    method: "get",
  });
}
