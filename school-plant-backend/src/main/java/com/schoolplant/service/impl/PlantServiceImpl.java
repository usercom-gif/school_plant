package com.schoolplant.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.schoolplant.dto.PlantAddRequest;
import com.schoolplant.dto.PlantQueryRequest;
import com.schoolplant.dto.PlantStatusRequest;
import com.schoolplant.dto.PlantUpdateRequest;
import com.schoolplant.entity.AdoptionApplication;
import com.schoolplant.entity.CareTask;
import com.schoolplant.entity.Plant;
import com.schoolplant.entity.User;
import com.schoolplant.mapper.AdoptionApplicationMapper;
import com.schoolplant.mapper.CareTaskMapper;
import com.schoolplant.mapper.PlantMapper;
import com.schoolplant.mapper.UserMapper;
import com.schoolplant.service.PlantService;
import com.schoolplant.vo.PlantExcelVO;
import com.schoolplant.vo.PlantVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Year;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {

    @Autowired
    private AdoptionApplicationMapper adoptionApplicationMapper;

    @Autowired
    private CareTaskMapper careTaskMapper;

    @Autowired
    private UserMapper userMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String ensureJsonArray(String input) {
        if (!StringUtils.hasText(input)) {
            return null;
        }
        input = input.trim();
        // Try to parse as JSON Array if it looks like one
        if (input.startsWith("[") && input.endsWith("]")) {
            try {
                objectMapper.readTree(input);
                return input;
            } catch (Exception ignored) {
                // Not valid JSON, fall through to wrap
            }
        }
        
        // Wrap as array
        try {
            return objectMapper.writeValueAsString(Collections.singletonList(input));
        } catch (Exception e) {
            // Fallback for simple string
            return "[\"" + input.replace("\"", "\\\"") + "\"]";
        }
    }

    @Override
    public Page<PlantVO> getPlantList(PlantQueryRequest request) {
        Page<Plant> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<Plant> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(Plant::getName, request.getKeyword())
                    .or().like(Plant::getSpecies, request.getKeyword())
                    .or().like(Plant::getLocationDescription, request.getKeyword()));
        }
        if (StringUtils.hasText(request.getSpecies())) {
            wrapper.eq(Plant::getSpecies, request.getSpecies());
        }
        if (StringUtils.hasText(request.getRegion())) {
            wrapper.like(Plant::getRegion, request.getRegion());
        }
        if (request.getCareDifficulty() != null) {
            wrapper.eq(Plant::getCareDifficulty, request.getCareDifficulty());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(Plant::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(Plant::getCreatedAt);

        Page<Plant> plantPage = this.page(page, wrapper);
        Page<PlantVO> voPage = new Page<>(plantPage.getCurrent(), plantPage.getSize(), plantPage.getTotal());
        List<PlantVO> voList = plantPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public PlantVO getPlantDetail(Long id) {
        Plant plant = this.getById(id);
        if (plant == null) {
            throw new RuntimeException("植物不存在");
        }
        // TODO: Load associated info (adopter, history tasks, abnormalities, achievements) if needed for detailed view
        // For now returning basic info as requested by `PlantVO` structure
        return convertToVO(plant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPlant(PlantAddRequest request) {
        checkUnique(request.getPlantCode(), request.getSpecies(), request.getLocationDescription(), null);

        Plant plant = new Plant();
        BeanUtils.copyProperties(request, plant);
        if (plant.getStatus() == null) {
            plant.setStatus("AVAILABLE");
        }
        
        // Ensure imageUrls is a valid JSON array or null if empty
        plant.setImageUrls(ensureJsonArray(request.getImageUrls()));

        plant.setCreatedBy(StpUtil.getLoginIdAsLong());
        
        // 自动填充发布者信息
        User user = userMapper.selectById(plant.getCreatedBy());
        if (user != null) {
            plant.setUserRealName(user.getRealName());
            plant.setUserPhone(user.getPhone());
        }
        
        this.save(plant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlant(PlantUpdateRequest request) {
        Plant plant = this.getById(request.getId());
        if (plant == null) {
            throw new RuntimeException("植物不存在");
        }
        
        // 权限检查：非管理员只能修改自己发布的植物
        if (!StpUtil.hasRole("ADMIN") && !plant.getCreatedBy().equals(StpUtil.getLoginIdAsLong())) {
            throw new RuntimeException("无权修改他人发布的植物信息");
        }

        checkUnique(request.getPlantCode(), request.getSpecies(), request.getLocationDescription(), request.getId());

        BeanUtils.copyProperties(request, plant);
        
        // Handle imageUrls JSON format
        plant.setImageUrls(ensureJsonArray(request.getImageUrls()));
        
        this.updateById(plant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlants(List<Long> ids) {
        if (!StpUtil.hasRole("ADMIN")) {
            // 普通用户只能删除自己的
            Long currentUserId = StpUtil.getLoginIdAsLong();
            for (Long id : ids) {
                Plant plant = this.getById(id);
                if (plant != null && !plant.getCreatedBy().equals(currentUserId)) {
                    throw new RuntimeException("无权删除他人发布的植物 (ID: " + id + ")");
                }
            }
        }
        
        for (Long id : ids) {
            // Check association
            Long adoptionCount = adoptionApplicationMapper.selectCount(new LambdaQueryWrapper<AdoptionApplication>().eq(AdoptionApplication::getPlantId, id));
            if (adoptionCount > 0) {
                throw new RuntimeException("植物ID " + id + " 已存在认养申请，无法删除");
            }
            Long taskCount = careTaskMapper.selectCount(new LambdaQueryWrapper<CareTask>().eq(CareTask::getPlantId, id));
            if (taskCount > 0) {
                throw new RuntimeException("植物ID " + id + " 已存在养护任务，无法删除");
            }
        }
        this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(PlantStatusRequest request) {
        Plant plant = this.getById(request.getId());
        if (plant == null) {
            throw new RuntimeException("植物不存在");
        }
        plant.setStatus(request.getStatus());
        this.updateById(plant);
    }

    @Override
    public List<PlantVO> exportPlants(PlantQueryRequest request) {
        LambdaQueryWrapper<Plant> wrapper = new LambdaQueryWrapper<>();
        // Reuse query logic... ideally extract method
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(Plant::getName, request.getKeyword())
                    .or().like(Plant::getSpecies, request.getKeyword())
                    .or().like(Plant::getLocationDescription, request.getKeyword()));
        }
        // ... (other filters)
        if (StringUtils.hasText(request.getSpecies())) {
            wrapper.eq(Plant::getSpecies, request.getSpecies());
        }
        if (StringUtils.hasText(request.getRegion())) {
            wrapper.like(Plant::getRegion, request.getRegion());
        }
        if (request.getCareDifficulty() != null) {
            wrapper.eq(Plant::getCareDifficulty, request.getCareDifficulty());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(Plant::getStatus, request.getStatus());
        }
        
        List<Plant> list = this.list(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importPlants(MultipartFile file) throws IOException {
        List<PlantExcelVO> list = EasyExcel.read(file.getInputStream()).head(PlantExcelVO.class).sheet().doReadSync();
        List<Plant> plants = new ArrayList<>();
        
        Long currentUserId = StpUtil.getLoginIdAsLong();
        User currentUser = userMapper.selectById(currentUserId);
        
        for (PlantExcelVO vo : list) {
            // Basic validation
            if (!StringUtils.hasText(vo.getPlantCode()) || !StringUtils.hasText(vo.getName()) || !StringUtils.hasText(vo.getSpecies()) || !StringUtils.hasText(vo.getLocationDescription())) {
                throw new RuntimeException("导入数据存在必填项缺失: " + vo);
            }
            
            // Check unique constraint in loop? Might be slow but safe
            checkUnique(vo.getPlantCode(), vo.getSpecies(), vo.getLocationDescription(), null);

            Plant plant = new Plant();
            BeanUtils.copyProperties(vo, plant);
            if (vo.getPlantingYear() != null) {
                plant.setPlantingYear(Year.of(vo.getPlantingYear()));
            }
            plant.setCreatedBy(currentUserId);
            
            if (currentUser != null) {
                plant.setUserRealName(currentUser.getRealName());
                plant.setUserPhone(currentUser.getPhone());
            }
            
            if (plant.getStatus() == null) plant.setStatus("AVAILABLE");
            if (plant.getCareDifficulty() == null) plant.setCareDifficulty(1); // Default Low
            plants.add(plant);
        }
        this.saveBatch(plants);
    }

    @Override
    public List<String> getAllSpecies() {
        return this.baseMapper.findAllSpecies();
    }

    private void checkUnique(String code, String species, String location, Long id) {
        if (this.count(new LambdaQueryWrapper<Plant>()
                .eq(Plant::getPlantCode, code)
                .ne(id != null, Plant::getId, id)) > 0) {
            throw new RuntimeException("植物编号 " + code + " 已存在");
        }
        if (this.count(new LambdaQueryWrapper<Plant>()
                .eq(Plant::getSpecies, species)
                .eq(Plant::getLocationDescription, location)
                .ne(id != null, Plant::getId, id)) > 0) {
            throw new RuntimeException("该位置已存在同品种植物: " + species + " @ " + location);
        }
    }

    private PlantVO convertToVO(Plant plant) {
        PlantVO vo = new PlantVO();
        BeanUtils.copyProperties(plant, vo);
        return vo;
    }
}
