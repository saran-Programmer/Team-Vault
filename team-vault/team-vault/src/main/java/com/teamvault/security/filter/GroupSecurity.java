package com.teamvault.security.filter;

import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.teamvault.models.CustomPrincipal;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.entity.GroupMember;
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
        
        if (currentUser == null) return false;

        if (hasRole(currentUser, UserRole.SUPER_ADMIN)) return true;

        if (!hasRole(currentUser, UserRole.ADMIN)) return false;
        
        return groupMemberRepository
                .findByUser_IdAndGroup_Id(currentUser.getUserId(), groupId)
                .filter(member -> member.getMembershipStatus() == MembershipStatus.ACTIVE)
                .map(member -> member.getUserPermissions()
                        .contains(UserGroupPermission.INVITE_USER)).orElse(false);
    }
    
    public boolean permissionUpdateAllowed(String groupMemberId) {

        CustomPrincipal currentUser = SecurityUtil.getCurrentUser();
        
        if (hasRole(currentUser, UserRole.SUPER_ADMIN)) {
        	
            return true;
        }
        
        if (currentUser == null) return false;
        
         Optional<GroupMember> groupMemberDoc = groupMemberRepository.findById(groupMemberId);
         
         if(groupMemberDoc.isEmpty()) return false;
         
         GroupMember groupMember = groupMemberDoc.get();
                        
        return groupMember.getMembershipStatus() == MembershipStatus.ACTIVE 
        		&& groupMember.getUserPermissions().contains(UserGroupPermission.MANAGE_USER_ROLES) ? true : false;

    }
    
    public boolean canUploadResource(String groupMemberId) {
    	
        CustomPrincipal currentUser = SecurityUtil.getCurrentUser();
        
        if (hasRole(currentUser, UserRole.SUPER_ADMIN)) {
        	
            return true;
        }
        
        if (currentUser == null) return false;
        
         Optional<GroupMember> groupMemberDoc = groupMemberRepository.findById(groupMemberId);
         
         if(groupMemberDoc.isEmpty()) return false;
         
         GroupMember groupMember = groupMemberDoc.get();
                        
        return groupMember.getMembershipStatus() == MembershipStatus.ACTIVE 
        		&& groupMember.getUserPermissions().contains(UserGroupPermission.WRITE_RESOURCE) ? true : false;
    }

    private boolean hasRole(CustomPrincipal principal, UserRole role) {
    	
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_" + role.name()));
    }

}
