package com.teamvault.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupInviteRequest {

	@NotBlank(message = "Target user ID cannot be blank")
	private String targetUserId;
	
    @Min(value = 1, message = "Expiration days must be at least 1")
    @Max(value = 15, message = "Expiration days cannot exceed 15")
	private int daysToExpire;
	
    @Size(max = 500, message = "Invite message cannot exceed 500 characters")
	private String inviteMessage;
}