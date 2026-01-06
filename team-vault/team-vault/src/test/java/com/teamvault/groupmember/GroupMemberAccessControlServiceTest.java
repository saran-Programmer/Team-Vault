package com.teamvault.groupmember;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.teamvault.entity.GroupMember;
import com.teamvault.enums.GroupMemberEventType;
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

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
    	
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUser)
                .thenReturn(principal("user-1", UserRole.ADMIN));
    }

    @AfterEach
    void tearDown() {
    	
        securityUtilMock.close();
    }

    private CustomPrincipal principal(String userId, UserRole role) {
    	
        return new CustomPrincipal(userId, "user", role.name(), List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    private GroupMember groupMember(String userId, String groupId, Set<UserGroupPermission> permissions, MembershipStatus status, boolean deleted) {
       
    	return GroupMember.builder()
                .user(UserVO.builder().id(userId).build())
                .group(GroupVO.builder().id(groupId).build())
                .userPermissions(permissions)
                .membershipStatus(status)
                .isGroupDeleted(deleted)
                .build();
    }

    @Test
    void canInviteUser_whenSuperAdmin_shouldReturnTrue() {
    	
        securityUtilMock.when(SecurityUtil::getCurrentUser)
                .thenReturn(principal("super", UserRole.SUPER_ADMIN));

        assertTrue(service.canInviteUser("group-1"));
        verifyNoInteractions(groupMemberQueryProcessor);
    }

    @Test
    void canInviteUser_whenActiveMemberWithPermission_shouldReturnTrue() {

        GroupMember member = groupMember("user-1", "group-1",
                Set.of(UserGroupPermission.INVITE_USER),
                MembershipStatus.ACTIVE, false);

        when(groupMemberQueryProcessor.getByUserIdAndGroupId("user-1", "group-1"))
                .thenReturn(Optional.of(member));

        assertTrue(service.canInviteUser("group-1"));
    }

    @Test
    void canInviteUser_whenPermissionMissing_shouldReturnFalse() {

        GroupMember member = groupMember("user-1", "group-1",
                Set.of(UserGroupPermission.READ_RESOURCE),
                MembershipStatus.ACTIVE, false);

        when(groupMemberQueryProcessor.getByUserIdAndGroupId("user-1", "group-1"))
                .thenReturn(Optional.of(member));

        assertFalse(service.canInviteUser("group-1"));
    }

    @Test
    void permissionUpdateAllowed_whenSuperAdmin_shouldReturnTrue() {
        securityUtilMock.when(SecurityUtil::getCurrentUser)
                .thenReturn(principal("super", UserRole.SUPER_ADMIN));

        assertTrue(service.permissionUpdateAllowed("gm-1"));
    }

    @Test
    void permissionUpdateAllowed_whenOwnerWithPermission_shouldReturnTrue() {

        GroupMember member = groupMember("user-1", "group-1",
                Set.of(UserGroupPermission.MANAGE_USER_ROLES),
                MembershipStatus.ACTIVE, false);

        when(groupMemberQueryProcessor.getGroupMemberById("gm-1"))
                .thenReturn(Optional.of(member));

        assertTrue(service.permissionUpdateAllowed("gm-1"));
    }

    @Test
    void canUploadResource_whenPermissionPresent_shouldReturnTrue() {

        GroupMember member = groupMember("user-1", "group-1",
                Set.of(UserGroupPermission.WRITE_RESOURCE),
                MembershipStatus.ACTIVE, false);

        when(groupMemberQueryProcessor.getGroupMemberById("gm-2"))
                .thenReturn(Optional.of(member));

        assertTrue(service.canUploadResource("gm-2"));
    }

    @Test
    void canRemoveGroupMember_whenInviterPermissionPresent_shouldReturnTrue() {

        GroupMember target = groupMember("user-2", "group-1",
                Set.of(), MembershipStatus.ACTIVE, false);

        GroupMember admin = groupMember("user-1", "group-1",
                Set.of(UserGroupPermission.INVITE_USER),
                MembershipStatus.ACTIVE, false);

        when(groupMemberQueryProcessor.getGroupMemberById("gm-target"))
                .thenReturn(Optional.of(target));

        when(groupMemberQueryProcessor.getByUserIdAndGroupId("user-1", "group-1"))
                .thenReturn(Optional.of(admin));

        assertTrue(service.canRemoveGroupMember("gm-target"));
    }

    @Test
    void canRemoveGroupMember_whenTargetMissing_shouldReturnFalse() {

        when(groupMemberQueryProcessor.getGroupMemberById("gm-x"))
                .thenReturn(Optional.empty());

        assertFalse(service.canRemoveGroupMember("gm-x"));
    }

    @Test
    void canPerformMembershipAction_whenValidInviteAccepted_shouldReturnTrue() {

        GroupMember member = groupMember("user-1", "group-1",
                Set.of(), MembershipStatus.PENDING, false);

        when(groupMemberQueryProcessor.getGroupMemberById("gm-1"))
                .thenReturn(Optional.of(member));

        assertTrue(service.canPerformMembershipAction("gm-1", GroupMemberEventType.INVITE_ACCEPTED));
    }

    @Test
    void canPerformMembershipAction_whenInvalidStatusForInviteAccepted_shouldReturnFalse() {

        GroupMember member = groupMember("user-1", "group-1",
                Set.of(), MembershipStatus.ACTIVE, false);

        when(groupMemberQueryProcessor.getGroupMemberById("gm-1"))
                .thenReturn(Optional.of(member));

        assertFalse(service.canPerformMembershipAction("gm-1", GroupMemberEventType.INVITE_ACCEPTED));
    }

    @Test
    void canPerformMembershipAction_whenMemberExitedValid_shouldReturnTrue() {

        GroupMember member = groupMember("user-1", "group-1",
                Set.of(), MembershipStatus.ACTIVE, false);

        when(groupMemberQueryProcessor.getGroupMemberById("gm-2"))
                .thenReturn(Optional.of(member));

        assertTrue(service.canPerformMembershipAction("gm-2", GroupMemberEventType.MEMBER_EXITED));
    }

    @Test
    void canPerformMembershipAction_whenGroupDeleted_shouldReturnFalse() {

        GroupMember member = groupMember("user-1", "group-1",
                Set.of(), MembershipStatus.ACTIVE, true);

        when(groupMemberQueryProcessor.getGroupMemberById("gm-3"))
                .thenReturn(Optional.of(member));

        assertFalse(service.canPerformMembershipAction("gm-3", GroupMemberEventType.MEMBER_EXITED));
    }

    @Test
    void canPerformMembershipAction_whenUserMismatch_shouldReturnFalse() {

        GroupMember member = groupMember("user-2", "group-1",
                Set.of(), MembershipStatus.ACTIVE, false);

        when(groupMemberQueryProcessor.getGroupMemberById("gm-4"))
                .thenReturn(Optional.of(member));

        assertFalse(service.canPerformMembershipAction("gm-4", GroupMemberEventType.MEMBER_EXITED));
    }
}
