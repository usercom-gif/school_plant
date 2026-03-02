package com.schoolplant.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class KnowledgePostAddRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String tag;
    // Images handled within content (rich text) or separate field if needed. 
    // Requirement says "upload images (max 3)", usually separate field for list display.
    // Let's assume rich text handles content images, but cover images might be separate or extracted.
    // For now simple fields.
}
