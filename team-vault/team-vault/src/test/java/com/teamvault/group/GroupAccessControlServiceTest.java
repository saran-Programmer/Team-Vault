package com.teamvault.group;

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
import com.teamvault.security.filter.GroupAccessControlService;
import com.teamvault.security.filter.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class GroupAccessControlServiceTest {

    @Mock
    private GroupMemberQueryProcessor groupMemberQueryProcessor;

    @InjectMocks
    private GroupAccessControlService groupAccessControlService;

    private MockedStatic<SecurityUtil> mockCurrentUser(CustomPrincipal principal) {
        MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class);
        mocked.when(SecurityUtil::getCurrentUser).thenReturn(principal);
        return mocked;
    }

    private CustomPrincipal createPrincipal(String userId, UserRole role) {
        return new CustomPrincipal(
                userId,
                "test-user",
                role.name(),
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }

    @Test
    void canDeleteGroup_whenUserIsSuperAdmin_shouldReturnTrue() {

        CustomPrincipal principal = createPrincipal("super-1", UserRole.SUPER_ADMIN);

        try (MockedStatic<SecurityUtil> ignored = mockCurrentUser(principal)) {

            boolean result = groupAccessControlService.canDeleteGroup("group-1");

            assertTrue(result);
            verifyNoInteractions(groupMemberQueryProcessor);
        }
    }

    @Test
    void canDeleteGroup_whenUserIsNotMember_shouldReturnFalse() {

        CustomPrincipal principal = createPrincipal("user-1", UserRole.ADMIN);

        try (MockedStatic<SecurityUtil> ignored = mockCurrentUser(principal)) {

            when(groupMemberQueryProcessor
                    .getByUserIdAndGroupId("user-1", "group-1"))
                    .thenReturn(Optional.empty());

            boolean result = groupAccessControlService.canDeleteGroup("group-1");

            assertFalse(result);

            verify(groupMemberQueryProcessor, times(1))
                    .getByUserIdAndGroupId("user-1", "group-1");
        }
    }

    @Test
    void canDeleteGroup_whenUserHasAdminPermissions_shouldReturnTrue() {

        CustomPrincipal principal = createPrincipal("user-2", UserRole.ADMIN);

        GroupMember groupMember = GroupMember.builder()
                .membershipStatus(MembershipStatus.ACTIVE)
                .isGroupDeleted(false)
                .userPermissions(UserGroupPermission.adminPermissions())
                .build();

        try (MockedStatic<SecurityUtil> ignored = mockCurrentUser(principal)) {

            when(groupMemberQueryProcessor
                    .getByUserIdAndGroupId("user-2", "group-1"))
                    .thenReturn(Optional.of(groupMember));

            boolean result = groupAccessControlService.canDeleteGroup("group-1");

            assertTrue(result);
        }
    }

    @Test
    void canDeleteGroup_whenUserLacksAdminPermissions_shouldReturnFalse() {

        CustomPrincipal principal = createPrincipal("user-3", UserRole.ADMIN);

        GroupMember groupMember = GroupMember.builder()
                .membershipStatus(MembershipStatus.ACTIVE)
                .isGroupDeleted(false)
                .userPermissions(Set.of(UserGroupPermission.READ_RESOURCE))
                .build();

        try (MockedStatic<SecurityUtil> ignored = mockCurrentUser(principal)) {

            when(groupMemberQueryProcessor
                    .getByUserIdAndGroupId("user-3", "group-1"))
                    .thenReturn(Optional.of(groupMember));

            boolean result = groupAccessControlService.canDeleteGroup("group-1");

            assertFalse(result);
        }
    }
}
