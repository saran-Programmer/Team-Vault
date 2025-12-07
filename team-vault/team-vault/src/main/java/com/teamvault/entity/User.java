package com.teamvault.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.teamvault.valueobject.NameVO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teamvault.enums.UserRole;
import com.teamvault.valueobject.ContactVO;
import com.teamvault.valueobject.CredentialsVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Document("user")
@NoArgsConstructor
@AllArgsConstructor
public class User {

	@Id
	private String id;
	
	private NameVO name;
	
	private CredentialsVO credentials;
	
	private ContactVO contact;
	
	private UserRole userRole;
	
	@CreatedDate
	private Instant createdDate;
	
	@LastModifiedDate
	private Instant lastUpdatedDate;
	
	@JsonIgnore
    public static UserRole getNextRole(UserRole role) {
        return switch (role) {
            case USER -> UserRole.ADMIN;
            case ADMIN -> UserRole.SUPER_ADMIN;
            default -> role;
        };
    }
	
	@JsonIgnore
	public static UserRole getPreviousRole(UserRole role) {
	    return switch (role) {
	        case SUPER_ADMIN -> UserRole.ADMIN;
	        case ADMIN -> UserRole.USER;
	        default -> role;
	    };
	}
}
