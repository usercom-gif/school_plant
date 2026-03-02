import request from '@/utils/request';

// 获取帖子列表
export function listPost(params: any) {
  return request({
    url: '/knowledge/post/list',
    method: 'get',
    params
  });
}

// 获取帖子详情
export function getPost(id: number) {
  return request({
    url: '/knowledge/post/' + id,
    method: 'get'
  });
}

// 发布帖子
export function addPost(data: any) {
  return request({
    url: '/knowledge/post',
    method: 'post',
    data
  });
}

// 修改帖子
export function updatePost(data: any) {
  return request({
    url: '/knowledge/post',
    method: 'put',
    data
  });
}

// 删除帖子
export function delPost(id: number) {
  return request({
    url: '/knowledge/post/' + id,
    method: 'delete'
  });
}

// 审核帖子
export function auditPost(id: number, pass: boolean, reason?: string) {
  return request({
    url: '/knowledge/post/audit',
    method: 'put',
    params: {
      id,
      pass,
      reason
    }
  });
}

// 点赞/取消点赞
export function toggleLike(id: number) {
  return request({
    url: '/knowledge/post/' + id + '/like',
    method: 'post'
  });
}

// 推荐/取消推荐
export function toggleFeature(id: number, isFeatured: boolean) {
  return request({
    url: '/knowledge/post/' + id + '/feature',
    method: 'put',
    params: {
      isFeatured
    }
  });
}

// 举报帖子
export function reportPost(id: number, reason: string) {
  return request({
    url: '/knowledge/post/' + id + '/report',
    method: 'post',
    params: {
      reason
    }
  });
}
