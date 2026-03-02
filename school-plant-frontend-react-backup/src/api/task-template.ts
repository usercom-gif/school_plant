import request from "@/utils/request";

// 获取模板列表
export function listTemplate(params: any) {
  return request({
    url: "/task/template/list",
    method: "get",
    params,
  });
}

// 获取模板详情
export function getTemplate(id: number) {
  return request({
    url: "/task/template/" + id,
    method: "get",
  });
}

// 新增模板
export function addTemplate(data: any) {
  return request({
    url: "/task/template",
    method: "post",
    data,
  });
}

// 修改模板
export function updateTemplate(data: any) {
  return request({
    url: "/task/template",
    method: "put",
    data,
  });
}

// 删除模板
export function delTemplate(ids: string) {
  return request({
    url: "/task/template/" + ids,
    method: "delete",
  });
}

// 修改模板状态
export function changeTemplateStatus(id: number, status: number) {
  return request({
    url: "/task/template/status",
    method: "put",
    data: {
      id,
      status,
    },
  });
}
