import request from "@/utils/request";

export interface CareTask {
  id: number;
  plantId: number;
  userId: number; // adopter_id
  taskTemplateId?: number;
  taskType: string;
  taskDescription: string;
  dueDate: string;
  completedDate?: string;
  status: "PENDING" | "COMPLETED" | "OVERDUE";
  imageUrl?: string;

  // VO fields
  plantName?: string;
  userName?: string;
  createdAt?: string;
}

export interface TaskQueryRequest {
  page?: number;
  size?: number;
  userId?: number;
  status?: string;
}

// User APIs
export function getMyTasks(params: {
  page?: number;
  size?: number;
  status?: string;
}) {
  return request<any, any>({
    url: "/task/my-tasks",
    method: "get",
    params,
  });
}

export function completeTask(data: { id: number; imageUrl: string }) {
  return request<any, void>({
    url: "/task/complete",
    method: "post",
    data,
  });
}

// Admin APIs
export function getTaskList(params: TaskQueryRequest) {
  return request<any, any>({
    url: "/task/list",
    method: "get",
    params,
  });
}

export function createTask(data: Partial<CareTask>) {
  return request<any, void>({
    url: "/task/create",
    method: "post",
    data,
  });
}

export function updateTask(data: Partial<CareTask>) {
  return request<any, void>({
    url: "/task/update",
    method: "put",
    data,
  });
}

export function deleteTask(ids: number[]) {
  return request<any, void>({
    url: `/task/${ids.join(",")}`,
    method: "delete",
  });
}
