package com.schoolplant.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.Year;

@Data
public class PlantVO {
    private Long id;
    private String plantCode;
    private String name;
    private String species;
    private String family;
    private String locationDescription;
    private String region;
    private Integer careDifficulty;
    private String status;
    private Year plantingYear;
    private String lightRequirement;
    private String waterRequirement;
    private String description;
    private String imageUrls;
    private String careTips;
    private String growthCycle;
    private String userRealName;
    private String userPhone;
    private Long number;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
