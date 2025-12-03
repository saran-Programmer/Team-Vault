package com.teamvault.event.model;

import com.teamvault.entity.User;
import com.teamvault.enums.UserRole;
import com.teamvault.models.CustomPrincipal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRoleChangeEvent {

    private final User targetUser;
    
    private final UserRole oldRole;
    
    private final UserRole newRole;
    
    private final String action; // PROMOTION or DEPROMOTION
    
    private final CustomPrincipal changedBy;
	
}