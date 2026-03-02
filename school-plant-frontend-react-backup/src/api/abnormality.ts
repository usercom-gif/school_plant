import request from '@/utils/request';

export function reportAbnormality(data: FormData) {
  return request({
    url: '/abnormality/report',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

export function getAbnormalityList(params: any) {
  return request({
    url: '/abnormality/list',
    method: 'get',
    params,
  });
}

export function assignAbnormality(data: any) {
  return request({
    url: '/abnormality/assign',
    method: 'post',
    params: data, // Controller uses @RequestParam
  });
}

export function resolveAbnormality(data: FormData) {
  return request({
    url: '/abnormality/resolve',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

export function getAbnormalityDetail(id: number) {
  return request({
    url: `/abnormality/${id}`,
    method: 'get',
  });
}
