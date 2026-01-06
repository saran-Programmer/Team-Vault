package com.teamvault.security.filter;

import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.teamvault.entity.GroupMember;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.enums.UserRole;
import com.teamvault.models.CustomPrincipal;
import com.teamvault.queryprocessor.GroupMemberQueryProcessor;

import lombok.RequiredArgsConstructor;

@Component("groupMemberAccessControlService")
@RequiredArgsConstructor
public class GroupMemberAccessControllService {

    private final GroupMemberQueryProcessor groupMemberQueryProcessor;
    
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
    
    private boolean hasRole(CustomPrincipal principal, UserRole role) {
    	
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_" + role.name()));
    }
}
