package com.teamvault.security.filter;

import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.teamvault.entity.GroupMember;
import com.teamvault.enums.UserRole;
import com.teamvault.models.CustomPrincipal;
import com.teamvault.queryprocessor.GroupMemberQueryProcessor;

import lombok.RequiredArgsConstructor;

@Component("groupAccessControlService")
@RequiredArgsConstructor
public class GroupAccessControlService {

    private final GroupMemberQueryProcessor groupMemberQueryProcessor;

    public boolean canDeleteGroup(String groupId) {

        CustomPrincipal currentUserPrincipal = SecurityUtil.getCurrentUser();

        if (hasRole(currentUserPrincipal, UserRole.SUPER_ADMIN)) {
            return true;
        }

        Optional<GroupMember> groupMemberDoc = groupMemberQueryProcessor.getByUserIdAndGroupId(currentUserPrincipal.getUserId(), groupId);

        if (groupMemberDoc.isEmpty()) {
            return false;
        }

        GroupMember groupMember = groupMemberDoc.get();

        return groupMember.hasAdminPermissions();
    }

    private boolean hasRole(CustomPrincipal principal, UserRole role) {

        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_" + role.name()));
    }
}
