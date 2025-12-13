package com.teamvault.DTO;

import java.util.Set;

import com.teamvault.enums.UserGroupPermission;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionUpdateRequest {

	 @NotEmpty(message = "User permissions must not be empty")
	private Set<UserGroupPermission> userPermissions;
}