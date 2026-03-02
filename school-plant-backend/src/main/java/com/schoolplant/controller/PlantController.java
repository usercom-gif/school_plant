package com.schoolplant.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.annotation.Log;
import com.schoolplant.common.R;
import com.schoolplant.dto.*;
import com.schoolplant.service.PlantService;
import com.schoolplant.vo.PlantExcelVO;
import com.schoolplant.vo.PlantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Plant Management", description = "植物信息管理")
@RestController
@RequestMapping("/plant")
public class PlantController {

    @Autowired
    private PlantService plantService;

    @Operation(summary = "获取植物列表")
    @SaCheckLogin
    @GetMapping("/list")
    public R<Page<PlantVO>> list(PlantQueryRequest request) {
        return R.ok(plantService.getPlantList(request));
    }

    @Operation(summary = "获取植物详情")
    @SaCheckLogin
    @GetMapping("/{id}")
    public R<PlantVO> getInfo(@PathVariable Long id) {
        return R.ok(plantService.getPlantDetail(id));
    }

    @Operation(summary = "新增植物")
    @SaCheckLogin
    @Log(module = "PLANT", title = "新增植物", operationType = "INSERT")
    @PostMapping
    public R<Void> add(@Validated @RequestBody PlantAddRequest request) {
        plantService.addPlant(request);
        return R.ok();
    }

    @Operation(summary = "修改植物")
    @SaCheckLogin
    @Log(module = "PLANT", title = "修改植物", operationType = "UPDATE")
    @PutMapping
    public R<Void> update(@Validated @RequestBody PlantUpdateRequest request) {
        plantService.updatePlant(request);
        return R.ok();
    }

    @Operation(summary = "删除植物")
    @SaCheckLogin
    @Log(module = "PLANT", title = "删除植物", operationType = "DELETE")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        plantService.deletePlants(ids);
        return R.ok();
    }

    @Operation(summary = "修改植物状态")
    @SaCheckLogin
    @Log(module = "PLANT", title = "修改植物状态", operationType = "UPDATE")
    @PutMapping("/status")
    public R<Void> changeStatus(@Validated @RequestBody PlantStatusRequest request) {
        plantService.changeStatus(request);
        return R.ok();
    }

    @Operation(summary = "导出植物")
    @SaCheckPermission("plant:export")
    @GetMapping("/export")
    public void export(HttpServletResponse response, PlantQueryRequest request) throws IOException {
        List<PlantVO> list = plantService.exportPlants(request);
        List<PlantExcelVO> exportList = list.stream().map(vo -> {
            PlantExcelVO excelVO = new PlantExcelVO();
            BeanUtils.copyProperties(vo, excelVO);
            // Year to Integer conversion if needed, but BeanUtils might handle if types match or compatible
            if (vo.getPlantingYear() != null) {
                excelVO.setPlantingYear(vo.getPlantingYear().getValue());
            }
            return excelVO;
        }).collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("植物数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), PlantExcelVO.class).sheet("植物列表").doWrite(exportList);
    }

    @Operation(summary = "导入植物")
    @SaCheckPermission("plant:import")
    @Log(title = "植物管理", operationType = "导入")
    @PostMapping("/import")
    public R<Void> importData(@RequestParam("file") MultipartFile file) throws Exception {
        plantService.importPlants(file);
        return R.ok();
    }

    @Operation(summary = "获取所有植物品种")
    @GetMapping("/species")
    public R<List<String>> getSpecies() {
        return R.ok(plantService.getAllSpecies());
    }
}
