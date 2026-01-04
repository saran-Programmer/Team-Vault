package com.teamvault.security.filter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.teamvault.entity.User;
import com.teamvault.enums.RoleChangeAction;
import com.teamvault.enums.UserRole;
import com.teamvault.models.CustomPrincipal;
import com.teamvault.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component("userAccessControlService")
@RequiredArgsConstructor
public class UserAccessControlService {
	
	private final UserRepository userRepository;
	
	public boolean canPromoteUser(String targetUserId) {
		
	    return canChangeUserRole(targetUserId, RoleChangeAction.PROMOTE);
	}

	public boolean canDepromoteUser(String targetUserId) {
		
	    return canChangeUserRole(targetUserId, RoleChangeAction.DEPROMOTE);
	}

	public boolean canChangeUserRole(String targetUserId, RoleChangeAction action) {

	    CustomPrincipal currentUser = SecurityUtil.getCurrentUser();

	    if (isCurrentAndTargetUserSame(currentUser.getUserId(), targetUserId)) {
	    	
	        return false;
	    }

	    Optional<User> targetUserDoc = userRepository.findById(targetUserId);
	    
	    if (targetUserDoc.isEmpty()) {
	    	
	        return false;
	    }

	    User targetUser = targetUserDoc.get();

	    UserRole currentUserRole = UserRole.valueOf(currentUser.getRole());
	    UserRole targetRole = targetUser.getUserRole();

	    if (action == RoleChangeAction.PROMOTE && targetRole == UserRole.SUPER_ADMIN) {
	    	
	        return false;
	    }

	    if (action == RoleChangeAction.DEPROMOTE && targetRole == UserRole.USER) {
	    	
	        return false;
	    }

	    return currentUserRole.getLevel() > targetRole.getLevel();
	}

	
	private boolean isCurrentAndTargetUserSame(String currentUser, String targetUserId) {
		
		return currentUser.equals(targetUserId);
	}
}
