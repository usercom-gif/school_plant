import request from "@/utils/request";

export interface PlantVO {
  id: number;
  plantCode?: string;
  name: string;
  species: string;
  family?: string;
  locationDescription: string;
  region?: string;
  careDifficulty?: number;
  plantingYear: number;
  status: string; // 'HEALTHY', 'SICK', 'MAINTENANCE', 'ADOPTED'
  imageUrls?: string;
  lightRequirement?: string;
  waterRequirement?: string;
  description?: string;
  careTips?: string;
  growthCycle?: string;
  adopterName?: string;
  userRealName?: string;
  userPhone?: string;
  createdAt?: string;
  createdBy?: number;
}

export interface PlantQueryRequest {
  page?: number;
  size?: number;
  name?: string;
  species?: string;
  status?: string;
  location?: string;
  region?: string;
}

export interface PlantAddRequest {
  plantCode: string;
  name: string;
  species: string;
  family?: string;
  locationDescription: string;
  region?: string;
  careDifficulty: number;
  plantingYear: number;
  lightRequirement?: string;
  waterRequirement?: string;
  description?: string;
  imageUrls?: string;
  careTips?: string;
  growthCycle?: string;
}

export interface PlantUpdateRequest extends PlantAddRequest {
  id: number;
  status?: string;
}

export function getPlantList(params: PlantQueryRequest) {
  return request<any, any>({
    url: "/plant/list",
    method: "get",
    params,
  });
}

export function getPlantDetail(id: number) {
  return request<any, PlantVO>({
    url: `/plant/${id}`,
    method: "get",
  });
}

export function addPlant(data: PlantAddRequest) {
  return request<any, void>({
    url: "/plant",
    method: "post",
    data,
  });
}

export function updatePlant(data: PlantUpdateRequest) {
  return request<any, void>({
    url: "/plant",
    method: "put",
    data,
  });
}

export function deletePlant(ids: number[]) {
  return request<any, void>({
    url: `/plant/${ids.join(",")}`,
    method: "delete",
  });
}

export function getPlantSpecies() {
  return request<any, string[]>({
    url: "/plant/species",
    method: "get",
  });
}
