package com.teamvault.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRoleChangeResponse {
	
    private String userId;
    
    private String userName;
    
    private String oldRole;
    
    private String newRole;
}