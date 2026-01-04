package com.teamvault.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.teamvault.DTO.GroupRequestDTO;
import com.teamvault.entity.Group;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.User;
import com.teamvault.enums.UserRole;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.exception.ResourceNotFoundException;
import com.teamvault.models.CustomPrincipal;
import com.teamvault.query.processor.GroupQueryProcessor;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.repository.GroupRepository;
import com.teamvault.security.filter.SecurityUtil;
import com.teamvault.service.GroupService;
import com.teamvault.service.UserService;

@ExtendWith(MockitoExtension.class)
class GroupCrudServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupQueryProcessor groupQueryProcessor;

    @Mock
    private UserService userService;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private GroupService groupService;

    @Test
    void createGroup_whenGroupTitleAlreadyExists_shouldThrowException() {

        Group existing = mock(Group.class);

        GroupRequestDTO request = GroupRequestDTO.builder()
                .title("Team A")
                .build();

        when(groupQueryProcessor.getConflictingGroup(request)).thenReturn(Optional.of(existing));

        assertThrows(InvalidActionException.class, () -> groupService.createGroup(request));

        verify(groupQueryProcessor, times(1)).getConflictingGroup(request);

        verify(groupRepository, never()).save(any());
        verify(groupMemberRepository, never()).save(any());
        verifyNoMoreInteractions(groupQueryProcessor);
    }

    @Test
    void createGroup_whenCurrentUserIsAdminUser_shouldThrowException() {

        GroupRequestDTO request = GroupRequestDTO.builder()
                .title("Team B")
                .adminUserId("user-1")
                .build();

        when(groupQueryProcessor.getConflictingGroup(request)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {

            var principal = mock(CustomPrincipal.class);
            when(principal.getUserId()).thenReturn("user-1");

            securityUtil.when(SecurityUtil::getCurrentUser)
                    .thenReturn(principal);

            assertThrows(InvalidActionException.class,
                    () -> groupService.createGroup(request));
        }

        verifyNoInteractions(userService);
        verify(groupRepository, never()).save(any());
        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    void createGroup_whenAssignedUserIsNotAdmin_shouldThrowException() {

        GroupRequestDTO request = GroupRequestDTO.builder()
                .title("Team C")
                .adminUserId("admin-1")
                .build();

        User nonAdminUser = mock(User.class);
        when(nonAdminUser.getUserRole()).thenReturn(UserRole.USER);

        when(groupQueryProcessor.getConflictingGroup(request))
                .thenReturn(Optional.empty());

        when(userService.getUserById("admin-1"))
                .thenReturn(nonAdminUser);

        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {

            var principal = mock(CustomPrincipal.class);
            when(principal.getUserId()).thenReturn("creator-1");

            securityUtil.when(SecurityUtil::getCurrentUser)
                    .thenReturn(principal);

            assertThrows(InvalidActionException.class,
                    () -> groupService.createGroup(request));
        }

        verify(groupRepository, never()).save(any());
        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    void createGroup_whenValidRequest_shouldCreateGroupAndAdminMember() {

        GroupRequestDTO request = GroupRequestDTO.builder()
                .title("Team D")
                .adminUserId("admin-1")
                .build();

        Group savedGroup = mock(Group.class);
        when(savedGroup.getId()).thenReturn("group-1");

        User adminUser = mock(User.class);
        when(adminUser.getUserRole()).thenReturn(UserRole.ADMIN);

        when(groupQueryProcessor.getConflictingGroup(request))
                .thenReturn(Optional.empty());

        when(userService.getUserById("admin-1"))
                .thenReturn(adminUser);

        when(groupRepository.save(any(Group.class)))
                .thenReturn(savedGroup);

        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {

            var principal = mock(CustomPrincipal.class);
            when(principal.getUserId()).thenReturn("creator-1");

            securityUtil.when(SecurityUtil::getCurrentUser)
                    .thenReturn(principal);

            groupService.createGroup(request);
        }

        verify(groupRepository, times(1))
                .save(any(Group.class));

        verify(groupMemberRepository, times(1))
                .save(any(GroupMember.class));
    }
    
    @Test
    void deleteGroup_whenGroupExists_shouldSoftDeleteGroupAndMembers() {

        Group group = mock(Group.class);
        when(group.isDeleted()).thenReturn(false);

        when(groupRepository.findById("group-1"))
                .thenReturn(Optional.of(group));

        groupService.deleteGroup("group-1");

        verify(group).setDeleted(true);

        verify(groupMemberRepository, times(1))
                .markGroupMembersAsDeleted("group-1");

        verify(groupRepository, times(1)).save(group);
    }
    
    
    @Test
    void getActiveGroupOrThrow_whenGroupNotFound_shouldThrowResourceNotFoundException() {

        String groupId = "group-1";
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class,
                        () -> groupService.getActiveGroupOrThrow(groupId));

        assertEquals("Group", exception.getResourceName());
        assertEquals(groupId, exception.getResourceId());

        verify(groupRepository, times(1)).findById(groupId);
    }

    @Test
    void getActiveGroupOrThrow_whenGroupIsDeleted_shouldThrowInvalidActionException() {

        String groupId = "group-2";
        Group deletedGroup = Group.builder()
                .id(groupId)
                .isDeleted(true)
                .build();

        when(groupRepository.findById(groupId))
                .thenReturn(Optional.of(deletedGroup));

        InvalidActionException exception =
                assertThrows(InvalidActionException.class,
                        () -> groupService.getActiveGroupOrThrow(groupId));

        assertEquals("Group " + groupId + " is deleted", exception.getMessage());

        verify(groupRepository, times(1)).findById(groupId);
    }

    @Test
    void getActiveGroupOrThrow_whenGroupIsActive_shouldReturnGroup() {

        String groupId = "group-3";
        Group activeGroup = Group.builder()
                .id(groupId)
                .isDeleted(false)
                .build();

        when(groupRepository.findById(groupId))
                .thenReturn(Optional.of(activeGroup));

        Group result = groupService.getActiveGroupOrThrow(groupId);

        assertNotNull(result);
        assertEquals(groupId, result.getId());
        assertFalse(result.isDeleted());

        verify(groupRepository, times(1)).findById(groupId);
    }
}

