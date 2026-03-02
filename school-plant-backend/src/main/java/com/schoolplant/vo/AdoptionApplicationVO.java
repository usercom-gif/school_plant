package com.schoolplant.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdoptionApplicationVO {
    private Long id;
    private Long userId;
    private Long plantId;
    
    // Join fields
    private String userName;
    private String plantName;
    
    private Integer adoptionPeriodMonths;
    private String careExperience; // Used as "Reason" in frontend often, or separate reason field?
                                   // Frontend asks for "reason", Entity has "care_experience".
                                   // We should map careExperience to reason or check if we need a reason field.
                                   // The entity only has care_experience.
    
    private String status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
