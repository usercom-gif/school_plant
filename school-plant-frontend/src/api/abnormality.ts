import request from '@/utils/request';

export interface PlantAbnormality {
  id: number;
  plantId: number;
  reporterId: number;
  maintainerId?: number;
  abnormalityType: string;
  description: string;
  imageUrls: string; // JSON string
  suggestedSolution?: string;
  status: 'PENDING' | 'ASSIGNED' | 'RESOLVED';
  assignedAt?: string;
  resolutionDescription?: string;
  resolutionImageUrls?: string; // JSON string
  materialsUsed?: string;
  effectEvaluation?: string;
  resolvedAt?: string;
  createdAt: string;
}

export interface ReportAbnormalityParams {
  plantId: number;
  type: string;
  desc: string;
  images?: File[];
}

export interface AbnormalityQueryParams {
  page?: number;
  size?: number;
  status?: string;
  maintainerId?: number;
  reporterId?: number;
}

export function reportAbnormality(data: ReportAbnormalityParams) {
  const formData = new FormData();
  formData.append('plantId', data.plantId.toString());
  formData.append('type', data.type);
  formData.append('desc', data.desc);
  if (data.images) {
    data.images.forEach((file) => {
      formData.append('images', file);
    });
  }
  return request<any, string>({
    url: '/abnormality/report',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

export function getAbnormalityList(params: AbnormalityQueryParams) {
  return request<any, any>({
    url: '/abnormality/list',
    method: 'get',
    params,
  });
}

export function assignAbnormality(id: number, maintainerId: number) {
  return request<any, void>({
    url: '/abnormality/assign',
    method: 'post',
    params: { id, maintainerId },
  });
}

export function resolveAbnormality(data: {
  id: number;
  resolution: string;
  materials: string;
  evaluation: string;
  images?: File[];
}) {
  const formData = new FormData();
  formData.append('id', data.id.toString());
  formData.append('resolution', data.resolution);
  formData.append('materials', data.materials);
  formData.append('evaluation', data.evaluation);
  if (data.images) {
    data.images.forEach((file) => {
      formData.append('images', file);
    });
  }
  return request<any, void>({
    url: '/abnormality/resolve',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

export function getAbnormalityDetail(id: number) {
  return request<any, PlantAbnormality>({
    url: `/abnormality/${id}`,
    method: 'get',
  });
}
