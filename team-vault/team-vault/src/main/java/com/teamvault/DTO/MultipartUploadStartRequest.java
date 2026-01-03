package com.teamvault.DTO;

import com.teamvault.enums.ResourceVisiblity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MultipartUploadStartRequest {

    @NotBlank(message = "Resource title is required")
    @Size(max = 20, message = "Title must be at most 20 characters")
    private String title;

    private String description;

    @NotNull(message = "File size in bytes is required")
    @Positive(message = "File size in bytes must be greater than 0")
    @Max(value = 500 * 1024 * 1024L, message = "File size in bytes must not exceed 500 MB")
    private Long fileSizeInBytes;

    @Builder.Default
    private ResourceVisiblity resourceVisiblity = ResourceVisiblity.PRIVATE;
}
