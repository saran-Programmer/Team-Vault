package com.teamvault.security.filter;

import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.teamvault.DTO.PresignedResourceResponse;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.Resource;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.ResourceVisiblity;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.enums.UserRole;
import com.teamvault.models.CustomPrincipal;
import com.teamvault.query.processor.GroupMemberQueryProcessor;
import com.teamvault.query.processor.ResourceQueryProcessor;

import lombok.RequiredArgsConstructor;

@Component("accessControlService")
@RequiredArgsConstructor
public class AccessControlService {

    private final GroupMemberQueryProcessor groupMemberQueryProcessor;
    
    private final ResourceQueryProcessor resourceQueryProcessor;
    
    private boolean hasPermission(String groupMemberId, UserGroupPermission permission) {
    	
        CustomPrincipal currentUser = SecurityUtil.getCurrentUser();
        
        if (currentUser == null) return false;

        if (hasRole(currentUser, UserRole.SUPER_ADMIN)) return true;

        return groupMemberQueryProcessor.getGroupMemberById(groupMemberId)
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

        return groupMemberQueryProcessor.getByUserIdAndGroupId(currentUser.getUserId(), groupId)
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
    
    public boolean canRemoveGroupMember(String groupMemberId) {
    	
    	 Optional<GroupMember> groupMemberOptional = groupMemberQueryProcessor.getGroupMemberById(groupMemberId);
    	 
    	 if(groupMemberOptional.isEmpty()) return false;
    	 
    	 GroupMember groupMember = groupMemberOptional.get();
    	 
    	 String currentUserId = SecurityUtil.getCurrentUser().getUserId();
    	 
    	 String groupId = groupMember.getGroup().getId();
    
    	 return groupMemberQueryProcessor.getByUserIdAndGroupId(currentUserId, groupId)
				    	 .filter(member ->
				         member.getMembershipStatus() == MembershipStatus.ACTIVE &&
				         !member.isGroupDeleted() && member.getUserPermissions().contains(UserGroupPermission.INVITE_USER)
				).isPresent();
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

        return groupMemberQueryProcessor.getGroupMemberById(resource.getGroupMemberId())
                .filter(member ->  member.getUser().getId().equals(currentUser.getUserId()) && 
		                member.getMembershipStatus() == MembershipStatus.ACTIVE &&
                        !member.isGroupDeleted() 
                        && member.getUserPermissions().contains(UserGroupPermission.READ_RESOURCE)).isPresent();
    }
    
    public boolean canModifyResource(String resourceId) {
    	
        CustomPrincipal currentUser = SecurityUtil.getCurrentUser();
        
        if (currentUser == null) return false;
        
        Optional<Resource> resourceDoc = resourceQueryProcessor.getResourceById(resourceId);
        
        if(resourceDoc.isEmpty()) return false;
        
        Resource resource = resourceDoc.get();
        
        return resource.getUser().getId().equals(currentUser.getUserId());
    }
    
	public boolean canInteractWithResource(String resourceId) {
		
        CustomPrincipal currentUser = SecurityUtil.getCurrentUser();
		
        if (hasRole(currentUser, UserRole.SUPER_ADMIN)) return true;

		Resource resource = resourceQueryProcessor.getResourceOrThrow(resourceId);

		String groupId = resource.getGroup().getId();

		String userId = currentUser.getUserId();

		GroupMember groupMember = groupMemberQueryProcessor.getByUserIdAndGroupId(userId, groupId)
				.filter(member -> member.getMembershipStatus() == MembershipStatus.ACTIVE && !member.isGroupDeleted())
				.orElse(null);

		if (groupMember == null) return false;

		return groupMember.getUserPermissions().contains(UserGroupPermission.WRITE_RESOURCE);
	}

    private boolean hasRole(CustomPrincipal principal, UserRole role) {
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_" + role.name()));
    }
}