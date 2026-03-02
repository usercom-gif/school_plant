package com.schoolplant.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class AdoptionApplyRequest {
    @NotNull(message = "植物ID不能为空")
    private Long plantId;

    @NotNull(message = "认养周期不能为空")
    private Integer adoptionPeriodMonths;

    @NotBlank(message = "认养承诺不能为空")
    @Size(min = 20, message = "认养承诺字数不能少于20字")
    private String careExperience; // Reusing this field for "Commitment/Experience" as per requirements

    @NotBlank(message = "联系方式不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;
}
