package com.schoolplant.dto;

import lombok.Data;

@Data
public class TemplateQueryRequest {
    private Integer page = 1;
    private Integer size = 10;
    private String plantSpecies;
    private String taskType;
    private Integer status;
}
