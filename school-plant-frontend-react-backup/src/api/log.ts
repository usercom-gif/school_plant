import request from '@/utils/request';

// 查询操作日志列表
export function listLogs(params: any) {
  return request({
    url: '/system/log/list',
    method: 'get',
    params
  });
}
