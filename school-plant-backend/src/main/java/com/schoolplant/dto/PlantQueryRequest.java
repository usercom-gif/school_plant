package com.schoolplant.dto;

import lombok.Data;
import java.time.Year;

@Data
public class PlantQueryRequest {
    private Integer page = 1;
    private Integer size = 10;
    private String keyword; // name or species or location
    private String species;
    private String region;
    private Integer careDifficulty;
    private String status;
}
