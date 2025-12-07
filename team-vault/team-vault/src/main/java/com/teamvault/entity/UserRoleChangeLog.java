package com.teamvault.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("user_role_change_log")
public class UserRoleChangeLog {

    @Id
    private String id;

    private String targetUserId;
    
    private String targetUserName;
    
    private String oldRole;
    
    private String newRole;
    
    private String action;
    
    private String changedByUserId;
    
    private String changedByUserName;
    
    private Instant timestamp;
}