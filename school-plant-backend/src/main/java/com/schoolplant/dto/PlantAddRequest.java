package com.schoolplant.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Year;

@Data
public class PlantAddRequest {
    @NotBlank(message = "植物编号不能为空")
    private String plantCode;

    @NotBlank(message = "植物名称不能为空")
    private String name;

    @NotBlank(message = "植物品种不能为空")
    private String species;

    private String family;

    @NotBlank(message = "位置描述不能为空")
    private String locationDescription;

    private String region;

    @NotNull(message = "养护难度不能为空")
    private Integer careDifficulty;

    private String status; // AVAILABLE, ADOPTED
    private Year plantingYear;
    private String lightRequirement;
    private String waterRequirement;
    private String description;
    private String imageUrls; // JSON Array
    private String careTips;
    private String growthCycle;
    private Long number;
}
