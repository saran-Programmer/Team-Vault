package com.teamvault.DTO;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {
	
    private String id;
    
    private String fullName;
    
    private String email;
    
    private String userRole;
    
    private Instant createdDate;
}
