import request from '@/utils/request';

// 获取认养申请列表 (Admin)
export function listApplications(params: any) {
  return request({
    url: '/adoption/audit/list',
    method: 'get',
    params
  });
}

// 审核认养申请
export function auditApplication(data: any) {
  return request({
    url: '/adoption/audit/action',
    method: 'post',
    data
  });
}

// 获取审核日志
export function listAuditLogs(applicationId: number) {
  return request({
    url: `/adoption/audit/logs/${applicationId}`,
    method: 'get'
  });
}

// 提交认养申请
export function submitAdoption(data: any) {
  return request({
    url: '/adoption/submit',
    method: 'post',
    data
  });
}

// 检查认养状态
export function checkStatus() {
  return request({
    url: '/adoption/status',
    method: 'get'
  });
}

// 我的认养记录 (Active)
export function getMyAdoptions(params: any) {
  return request({
    url: '/adoption/my',
    method: 'get',
    params
  });
}

// 我的申请记录 (All)
export function getMyApplications(params: any) {
  return request({
    url: '/adoption/my-applications',
    method: 'get',
    params
  });
}

// 取消认养
export function cancelAdoption(recordId: number, reason: string) {
  return request({
    url: '/adoption/cancel',
    method: 'post',
    params: { recordId, reason }
  });
}
