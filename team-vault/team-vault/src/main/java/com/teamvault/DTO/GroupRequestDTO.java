package com.teamvault.DTO;

import com.teamvault.enums.GroupVisibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupRequestDTO {

    @NotBlank(message = "Group title is required")
    @Size(max = 20, message = "Title must be at most 20 characters")
    private String title;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;
    
    @NotBlank
    private String adminUserId;

    @Builder.Default
    private GroupVisibility groupVisibility = GroupVisibility.PRIVATE;
}
