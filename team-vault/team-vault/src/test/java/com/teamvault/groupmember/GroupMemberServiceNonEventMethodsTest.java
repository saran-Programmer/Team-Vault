package com.teamvault.groupmember;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.teamvault.DTO.PermissionUpdateRequest;
import com.teamvault.DTO.PermissionUpdateResponse;
import com.teamvault.entity.GroupMember;
import com.teamvault.enums.GroupMemberSortField;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.SortDirection;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.enums.UserRole;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.exception.ResourceNotFoundException;
import com.teamvault.queryprocessor.GroupMemberQueryProcessor;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.repository.GroupRepository;
import com.teamvault.security.filter.SecurityUtil;
import com.teamvault.service.GroupMemberService;
import com.teamvault.service.GroupService;
import com.teamvault.valueobject.GroupVO;
import com.teamvault.valueobject.UserVO;
import com.teamvault.models.CustomPrincipal;

@ExtendWith(MockitoExtension.class)
class GroupMemberServiceNonEventMethodsTest {

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private GroupMemberQueryProcessor groupMemberQueryProcessor;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupMemberService groupMemberService;

    @Test
    void updateUserPermission_shouldUpdatePermissionsAndPublishEvent() {
    	
        GroupMember groupMember = GroupMember.builder()
                .id("gm-1")
                .group(GroupVO.builder().id("group-1").build())
                .user(UserVO.builder().id("user-1").build()) 
                .userPermissions(Set.of(UserGroupPermission.READ_RESOURCE))
                .isGroupDeleted(false)
                .build();

        PermissionUpdateRequest request = PermissionUpdateRequest.builder()
                .userPermissions(Set.of(UserGroupPermission.WRITE_RESOURCE))
                .build();

        when(groupMemberQueryProcessor.getGroupMemberById("gm-1"))
                .thenReturn(Optional.of(groupMember));

        PermissionUpdateResponse response =
                groupMemberService.updateUserPermission("gm-1", request);

        verify(groupService).getActiveGroupOrThrow("group-1");
        verify(groupMemberRepository).save(groupMember);
        verify(eventPublisher).publishEvent(groupMember);

        assertNotNull(response);
        assertEquals(Set.of(UserGroupPermission.WRITE_RESOURCE), groupMember.getUserPermissions());
    }

    @Test
    void updateUserPermission_shouldThrowIfGroupMemberNotFound() {
        when(groupMemberQueryProcessor.getGroupMemberById("gm-404"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> groupMemberService.updateUserPermission("gm-404",
                        PermissionUpdateRequest.builder().build()));
    }

    @Test
    void updateUserPermission_shouldThrowIfGroupDeleted() {
        GroupMember groupMember = GroupMember.builder()
                .id("gm-1")
                .group(GroupVO.builder().id("group-1").build())
                .user(UserVO.builder().id("user-1").build())
                .isGroupDeleted(true)
                .build();

        when(groupMemberQueryProcessor.getGroupMemberById("gm-1"))
                .thenReturn(Optional.of(groupMember));

        assertThrows(InvalidActionException.class,
                () -> groupMemberService.updateUserPermission("gm-1",
                        PermissionUpdateRequest.builder().build()));
    }

    @Test
    void getUserActiveGroup_shouldDelegateToQueryProcessor() {
        when(groupMemberQueryProcessor.getUserActiveGroup(
                "user-1",
                UserRole.USER,
                0,
                10,
                GroupMemberSortField.LAST_ACCESSED,
                SortDirection.ASC))
                .thenReturn(java.util.List.of());

        try (var mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserRole).thenReturn(UserRole.USER);
            mocked.when(SecurityUtil::getCurrentUser)
            .thenReturn(new CustomPrincipal("user-1", "user-1", "ROLE_USER", Collections.emptyList()));

            assertNotNull(
                    groupMemberService.getUserActiveGroup(
                            0,
                            10,
                            GroupMemberSortField.LAST_ACCESSED,
                            SortDirection.ASC));
        }
    }

    @Test
    void removeGroupMember_shouldMarkMemberAsRemoved() {
        GroupMember groupMember = GroupMember.builder()
                .id("gm-1")
                .membershipStatus(MembershipStatus.ACTIVE)
                .build();

        when(groupMemberQueryProcessor.getGroupMemberById("gm-1"))
                .thenReturn(Optional.of(groupMember));

        groupMemberService.removeGroupMember("gm-1");

        assertEquals(MembershipStatus.REMOVED, groupMember.getMembershipStatus());
        verify(groupMemberQueryProcessor).saveUpdatedGroupMember(groupMember);
    }

    @Test
    void removeGroupMember_shouldThrowIfNotFound() {
        when(groupMemberQueryProcessor.getGroupMemberById("gm-404"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> groupMemberService.removeGroupMember("gm-404"));
    }

    @Test
    void getActiveGroupMemberOrThrow_shouldReturnGroupMember() {
        GroupMember groupMember = GroupMember.builder()
                .id("gm-1")
                .group(GroupVO.builder().id("group-1").build())
                .isGroupDeleted(false)
                .build();

        when(groupMemberQueryProcessor.getGroupMemberById("gm-1"))
                .thenReturn(Optional.of(groupMember));

        GroupMember result =
                groupMemberService.getActiveGroupMemberOrThrow("gm-1");

        assertEquals("gm-1", result.getId());
    }

    @Test
    void getActiveGroupMemberOrThrow_shouldThrowIfDeleted() {
        GroupMember groupMember = GroupMember.builder()
                .id("gm-1")
                .group(GroupVO.builder().id("group-1").build())
                .isGroupDeleted(true)
                .build();

        when(groupMemberQueryProcessor.getGroupMemberById("gm-1"))
                .thenReturn(Optional.of(groupMember));

        assertThrows(InvalidActionException.class,
                () -> groupMemberService.getActiveGroupMemberOrThrow("gm-1"));
    }

    @Test
    void getActiveGroupMemberOrThrow_shouldThrowIfNotFound() {
        when(groupMemberQueryProcessor.getGroupMemberById("gm-404"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> groupMemberService.getActiveGroupMemberOrThrow("gm-404"));
    }
}
