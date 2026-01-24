package com.teamvault.security.filter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.teamvault.DTO.PresignedResourceResponse;
import com.teamvault.entity.Resource;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.ResourceVisiblity;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.enums.UserRole;
import com.teamvault.models.CustomPrincipal;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.repository.ResourceRepository;

import lombok.RequiredArgsConstructor;

@Component("groupSecurity")
@RequiredArgsConstructor
public class GroupSecurity {

    private final GroupMemberRepository groupMemberRepository;
    
    private final ResourceRepository resourceRepository;
    
    private boolean hasPermission(String groupMemberId, UserGroupPermission permission) {
    	
        CustomPrincipal currentUser = SecurityUtil.getCurrentUser();
        
        if (currentUser == null) return false;

        if (hasRole(currentUser, UserRole.SUPER_ADMIN)) return true;

        return groupMemberRepository.findById(groupMemberId)
                .filter(member ->
                		member.getUser().getId().equals(currentUser.getUserId()) && 
                        member.getMembershipStatus() == MembershipStatus.ACTIVE &&
                        !member.isGroupDeleted() &&
                        member.getUserPermissions().contains(permission)
                ).isPresent();
    }

    public boolean canInviteUser(String groupId) {
    	
        CustomPrincipal currentUser = SecurityUtil.getCurrentUser();
        
        if (currentUser == null) return false;

        if (hasRole(currentUser, UserRole.SUPER_ADMIN)) return true;

        if (!hasRole(currentUser, UserRole.ADMIN)) return false;

        return groupMemberRepository.findByUser_IdAndGroup_Id(currentUser.getUserId(), groupId)
                .filter(member ->
                        member.getMembershipStatus() == MembershipStatus.ACTIVE &&
                        !member.isGroupDeleted() &&
                        member.getUserPermissions().contains(UserGroupPermission.INVITE_USER)
                ).isPresent();
    }

    public boolean permissionUpdateAllowed(String groupMemberId) {
    	
        return hasPermission(groupMemberId, UserGroupPermission.MANAGE_USER_ROLES);
    }

    public boolean canUploadResource(String groupMemberId) {
    	
        return hasPermission(groupMemberId, UserGroupPermission.WRITE_RESOURCE);
    }
    
    public boolean canAccessGroupResources(String groupMemberId) {
    	
        return hasPermission(groupMemberId, UserGroupPermission.READ_RESOURCE);
    }
    
    public boolean canViewResource(PresignedResourceResponse resource) {

        if (resource == null) return false;

        CustomPrincipal currentUser = SecurityUtil.getCurrentUser();
        
        if (currentUser == null) return false;

        String currentUserId = currentUser.getUserId();

        if (hasRole(currentUser, UserRole.SUPER_ADMIN)) return true;

        if (resource.getResourceVisiblity() == ResourceVisiblity.PRIVATE || resource.getResourceVisiblity() == ResourceVisiblity.ARCHIVED) {
        	
            return resource.getResourceOwnerId().equals(currentUserId);
        }

        return groupMemberRepository.findById(resource.getGroupMemberId())
                .filter(member ->  member.getUser().getId().equals(currentUser.getUserId()) && 
		                member.getMembershipStatus() == MembershipStatus.ACTIVE &&
                        !member.isGroupDeleted() 
                        && member.getUserPermissions().contains(UserGroupPermission.READ_RESOURCE)).isPresent();
    }
    

    private boolean hasRole(CustomPrincipal principal, UserRole role) {
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_" + role.name()));
    }
}
