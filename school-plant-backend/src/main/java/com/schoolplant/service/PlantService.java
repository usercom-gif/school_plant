package com.schoolplant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.schoolplant.dto.*;
import com.schoolplant.entity.Plant;
import com.schoolplant.vo.PlantVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PlantService extends IService<Plant> {
    Page<PlantVO> getPlantList(PlantQueryRequest request);
    PlantVO getPlantDetail(Long id);
    void addPlant(PlantAddRequest request);
    void updatePlant(PlantUpdateRequest request);
    void deletePlants(List<Long> ids);
    void changeStatus(PlantStatusRequest request);
    List<PlantVO> exportPlants(PlantQueryRequest request);
    void importPlants(MultipartFile file) throws IOException;
    List<String> getAllSpecies();
}
