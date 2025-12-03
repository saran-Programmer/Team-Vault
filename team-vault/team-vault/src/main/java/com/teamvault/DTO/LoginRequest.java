package com.teamvault.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginRequest {

	private String userName;
	
	private String emailAddress;
	
	@NotBlank(message = "Password is required")
	private String password;
}
