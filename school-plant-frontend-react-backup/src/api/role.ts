import request from '@/utils/request';

// 获取角色列表
export function listRole(params: any) {
  return request({
    url: '/system/role/list',
    method: 'get',
    params
  });
}

// 获取角色详情
export function getRole(id: number) {
  return request({
    url: '/system/role/' + id,
    method: 'get'
  });
}

// 新增角色
export function addRole(data: any) {
  return request({
    url: '/system/role',
    method: 'post',
    data
  });
}

// 修改角色
export function updateRole(data: any) {
  return request({
    url: '/system/role',
    method: 'put',
    data
  });
}

// 删除角色
export function delRole(ids: string) {
  return request({
    url: '/system/role/' + ids,
    method: 'delete'
  });
}

// 修改角色状态
export function changeRoleStatus(id: number, status: number) {
  return request({
    url: '/system/role/status',
    method: 'put',
    data: {
      id,
      status
    }
  });
}

// 导出角色
export function exportRole(params: any) {
  return request({
    url: '/system/role/export',
    method: 'get',
    params,
    responseType: 'blob'
  });
}
