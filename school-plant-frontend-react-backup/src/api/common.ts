import request from '@/utils/request';

// 上传文件
export function uploadFile(data: FormData) {
  return request({
    url: '/common/upload',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}
