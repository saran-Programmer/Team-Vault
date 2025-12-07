package com.teamvault.security.filter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.teamvault.models.CustomPrincipal;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.enums.UserRole;

import lombok.RequiredArgsConstructor;

@Component("groupSecurity")
@RequiredArgsConstructor
public class GroupSecurity {

    private final GroupMemberRepository groupMemberRepository;

    public boolean canInviteUser(String groupId) {
    	
        CustomPrincipal currentUser = SecurityUtil.getCurrentUser();
        
        if (currentUser == null || currentUser.getAuthorities() == null) return false;

        for (GrantedAuthority authority : currentUser.getAuthorities()) {
        	
            if (("ROLE_" + UserRole.SUPER_ADMIN.toString()).equals(authority.getAuthority())) {
            	
                return true;
            }
        }

        boolean isAdmin = false;
        
        for (GrantedAuthority authority : currentUser.getAuthorities()) {
        	
            if (("ROLE_" + UserRole.ADMIN.toString()).equals(authority.getAuthority())) {
            	
                isAdmin = true;
                break;
            }
        }
        
        if (!isAdmin) return false;

        return groupMemberRepository.findByUser_IdAndGroup_Id(currentUser.getUserId(), groupId)
                .filter(m -> m.getMembershipStatus() == MembershipStatus.ACTIVE)
                .map(m -> m.getUserPermissions().contains(UserGroupPermission.INVITE_USER))
                .orElse(false);
    }
}
