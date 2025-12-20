package com.greenfox.backend.modules.resort.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for uploaded photo information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Photo upload response")
public class PhotoUploadResponse {
    
    @Schema(description = "Photo URL")
    private String url;
    
    @Schema(description = "Photo description")
    private String description;
    
    @Schema(description = "Photo order index")
    private Integer order;
}

