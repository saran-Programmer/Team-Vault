package com.teamvault.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
	
    @NotBlank(message = "First name is required")
    private String firstName;

    private String middleName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 20, message = "Username must be 4-20 characters")
    private String username;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String primaryEmail;

    @NotBlank(message = "Country code is required")
    private String phoneCountryCode;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Email(message = "Invalid secondary email")
    private String secondaryEmail;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
