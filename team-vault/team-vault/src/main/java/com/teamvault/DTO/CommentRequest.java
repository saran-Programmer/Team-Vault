package com.teamvault.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {

	@NotBlank(message = "Comment Cannot Be Blank")
    @Size(max = 2200, message = "Comment must be between 1 and 2200 characters")
	private String comment;
}