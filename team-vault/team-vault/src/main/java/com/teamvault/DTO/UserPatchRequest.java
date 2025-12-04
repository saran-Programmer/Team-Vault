package com.teamvault.DTO;

import lombok.Data;

@Data
public class UserPatchRequest {
	
    private String firstName;
    
    private String lastName;
    
    private String email;
    
    private String userName;
}