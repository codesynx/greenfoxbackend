package com.greenfox.backend.modules.resort.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding a photo to a resort.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resort photo upload request")
public class ResortPhotoRequest {

    @NotBlank(message = "Photo URL is required")
    @Schema(description = "URL of the uploaded photo")
    private String url;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    @Schema(description = "Photo description", example = "Bedroom with mountain view")
    private String description;

    @Schema(description = "Display order (0 = main photo)")
    private Integer order;
}

