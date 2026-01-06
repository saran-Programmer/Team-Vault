package com.teamvault.groupmember;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.teamvault.entity.GroupMember;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.enums.UserRole;
import com.teamvault.models.CustomPrincipal;
import com.teamvault.queryprocessor.GroupMemberQueryProcessor;
import com.teamvault.security.filter.GroupMemberAccessControllService;
import com.teamvault.security.filter.SecurityUtil;
import com.teamvault.valueobject.GroupVO;
import com.teamvault.valueobject.UserVO;

@ExtendWith(MockitoExtension.class)
class GroupMemberAccessControlServiceTest {

    @Mock
    private GroupMemberQueryProcessor groupMemberQueryProcessor;

    @InjectMocks
    private GroupMemberAccessControllService service;


    private CustomPrincipal principal(String userId, UserRole role) {
    	
        return new CustomPrincipal(userId, "user", role.name(), List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    private GroupMember groupMember(String userId, String groupId,  Set<UserGroupPermission> permissions, MembershipStatus status, boolean deleted) {
    	
        return GroupMember.builder()
                .user(UserVO.builder().id(userId).build())
                .group(GroupVO.builder().id(groupId).build())
                .userPermissions(permissions)
                .membershipStatus(status)
                .isGroupDeleted(deleted)
                .build();
    }

    private MockedStatic<SecurityUtil> mockUser(CustomPrincipal principal) {
    	
        MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class);
        mocked.when(SecurityUtil::getCurrentUser).thenReturn(principal);
        return mocked;
    }


    @Test
    void canInviteUser_whenSuperAdmin_shouldReturnTrue() {
    	
        try (var ignored = mockUser(principal("super", UserRole.SUPER_ADMIN))) {
            assertTrue(service.canInviteUser("group-1"));
            verifyNoInteractions(groupMemberQueryProcessor);
        }
    }

    @Test
    void canInviteUser_whenActiveMemberWithPermission_shouldReturnTrue() {
    	
        try (var ignored = mockUser(principal("user-1", UserRole.ADMIN))) {

            GroupMember member = groupMember(
                    "user-1",
                    "group-1",
                    Set.of(UserGroupPermission.INVITE_USER),
                    MembershipStatus.ACTIVE,
                    false
            );

            when(groupMemberQueryProcessor.getByUserIdAndGroupId("user-1", "group-1"))
                    .thenReturn(Optional.of(member));

            assertTrue(service.canInviteUser("group-1"));
        }
    }

    @Test
    void canInviteUser_whenPermissionMissing_shouldReturnFalse() {
        try (var ignored = mockUser(principal("user-1", UserRole.ADMIN))) {

            GroupMember member = groupMember(
                    "user-1",
                    "group-1",
                    Set.of(UserGroupPermission.READ_RESOURCE),
                    MembershipStatus.ACTIVE,
                    false
            );

            when(groupMemberQueryProcessor.getByUserIdAndGroupId("user-1", "group-1"))
                    .thenReturn(Optional.of(member));

            assertFalse(service.canInviteUser("group-1"));
        }
    }


    @Test
    void permissionUpdateAllowed_whenSuperAdmin_shouldReturnTrue() {
        try (var ignored = mockUser(principal("super", UserRole.SUPER_ADMIN))) {
            assertTrue(service.permissionUpdateAllowed("gm-1"));
        }
    }

    @Test
    void permissionUpdateAllowed_whenOwnerWithPermission_shouldReturnTrue() {
        try (var ignored = mockUser(principal("user-1", UserRole.ADMIN))) {

            GroupMember member = groupMember(
                    "user-1",
                    "group-1",
                    Set.of(UserGroupPermission.MANAGE_USER_ROLES),
                    MembershipStatus.ACTIVE,
                    false
            );

            when(groupMemberQueryProcessor.getGroupMemberById("gm-1"))
                    .thenReturn(Optional.of(member));

            assertTrue(service.permissionUpdateAllowed("gm-1"));
        }
    }

    @Test
    void canUploadResource_whenPermissionPresent_shouldReturnTrue() {
        try (var ignored = mockUser(principal("user-1", UserRole.ADMIN))) {

            GroupMember member = groupMember(
                    "user-1",
                    "group-1",
                    Set.of(UserGroupPermission.WRITE_RESOURCE),
                    MembershipStatus.ACTIVE,
                    false
            );

            when(groupMemberQueryProcessor.getGroupMemberById("gm-2"))
                    .thenReturn(Optional.of(member));

            assertTrue(service.canUploadResource("gm-2"));
        }
    }

    @Test
    void canRemoveGroupMember_whenInviterPermissionPresent_shouldReturnTrue() {
        try (var ignored = mockUser(principal("admin-1", UserRole.ADMIN))) {

            GroupMember target = groupMember(
                    "user-2",
                    "group-1",
                    Set.of(),
                    MembershipStatus.ACTIVE,
                    false
            );

            GroupMember admin = groupMember(
                    "admin-1",
                    "group-1",
                    Set.of(UserGroupPermission.INVITE_USER),
                    MembershipStatus.ACTIVE,
                    false
            );

            when(groupMemberQueryProcessor.getGroupMemberById("gm-target"))
                    .thenReturn(Optional.of(target));

            when(groupMemberQueryProcessor.getByUserIdAndGroupId("admin-1", "group-1"))
                    .thenReturn(Optional.of(admin));

            assertTrue(service.canRemoveGroupMember("gm-target"));
        }
    }

    @Test
    void canRemoveGroupMember_whenTargetMissing_shouldReturnFalse() {
        try (var ignored = mockUser(principal("admin", UserRole.ADMIN))) {

            when(groupMemberQueryProcessor.getGroupMemberById("gm-x"))
                    .thenReturn(Optional.empty());

            assertFalse(service.canRemoveGroupMember("gm-x"));
        }
    }
}
