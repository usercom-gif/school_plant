import request from '@/utils/request';

// 查询植物列表
export function listPlants(params: any) {
  return request({
    url: '/plant/list',
    method: 'get',
    params
  });
}

// 获取植物详情
export function getPlant(id: number) {
  return request({
    url: `/plant/${id}`,
    method: 'get'
  });
}

// 获取所有品种列表 (用于筛选)
export function getSpeciesList() {
  return request({
    url: '/plant/species',
    method: 'get'
  });
}
