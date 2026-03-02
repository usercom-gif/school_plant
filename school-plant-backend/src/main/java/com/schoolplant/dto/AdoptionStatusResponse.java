package com.schoolplant.dto;

import lombok.Data;

@Data
public class AdoptionStatusResponse {
    private boolean canAdopt;
    private String message;
}
