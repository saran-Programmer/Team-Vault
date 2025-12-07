package com.teamvault.security.filter;

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
        if (currentUser == null) return false;

        boolean isSuperAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRole.SUPER_ADMIN.toString()));
        if (isSuperAdmin) return true;

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRole.ADMIN.toString()));
        if (!isAdmin) return false;

        return groupMemberRepository.findByUser_IdAndGroup_Id(currentUser.getUserId(), groupId)
                .filter(m -> m.getMembershipStatus() == MembershipStatus.ACTIVE)
                .map(m -> m.getUserPermissions().contains(UserGroupPermission.INVITE_USER))
                .orElse(false);
    }
}
