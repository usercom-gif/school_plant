import request from '@/utils/request';

export interface KnowledgePostVO {
  id: number;
  authorId: number;
  authorName: string;
  authorAvatar: string;
  title: string;
  content: string;
  tag: string;
  isFeatured: number;
  likeCount: number;
  status: string;
  createdAt: string;
  hasLiked: boolean;
}

export interface KnowledgePostQueryRequest {
  page?: number;
  size?: number;
  keyword?: string;
  tag?: string;
  status?: string;
  authorId?: number;
}

export interface KnowledgePostAddRequest {
  title: string;
  content: string;
  tag?: string;
}

export interface KnowledgePostUpdateRequest {
  id: number;
  title?: string;
  content?: string;
  tag?: string;
}

export function listPosts(params: KnowledgePostQueryRequest) {
  return request({
    url: '/knowledge/post/list',
    method: 'get',
    params
  });
}

export function getPost(id: number) {
  return request({
    url: `/knowledge/post/${id}`,
    method: 'get'
  });
}

export function createPost(data: KnowledgePostAddRequest) {
  return request({
    url: '/knowledge/post',
    method: 'post',
    data
  });
}

export function updatePost(data: KnowledgePostUpdateRequest) {
  return request({
    url: '/knowledge/post',
    method: 'put',
    data
  });
}

export function deletePost(id: number) {
  return request({
    url: `/knowledge/post/${id}`,
    method: 'delete'
  });
}

export function likePost(id: number) {
  return request({
    url: `/knowledge/post/${id}/like`,
    method: 'post'
  });
}

export function reportPost(id: number, reason: string) {
  return request({
    url: `/knowledge/post/${id}/report`,
    method: 'post',
    params: { reason }
  });
}

export function auditPost(id: number, pass: boolean, reason?: string) {
  return request({
    url: '/knowledge/post/audit',
    method: 'put',
    params: { id, pass, reason }
  });
}

export function toggleFeature(id: number, isFeatured: boolean) {
  return request({
    url: `/knowledge/post/${id}/feature`,
    method: 'put',
    params: { isFeatured }
  });
}
