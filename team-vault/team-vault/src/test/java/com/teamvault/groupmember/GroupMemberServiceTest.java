package com.teamvault.groupmember;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.teamvault.DTO.GroupInviteRequest;
import com.teamvault.DTO.GroupMembershipResponse;
import com.teamvault.entity.Group;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.log.GroupMemberLog;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.queryprocessor.GroupMemberQueryProcessor;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.repository.GroupRepository;
import com.teamvault.security.filter.SecurityUtil;
import com.teamvault.service.GroupMemberService;
import com.teamvault.service.GroupService;
import com.teamvault.valueobject.GroupStatisticsVO;
import com.teamvault.valueobject.GroupVO;
import com.teamvault.valueobject.UserVO;
import com.teamvault.event.resolver.GroupMemberEventResolver;
import com.teamvault.models.CustomPrincipal;

@ExtendWith(MockitoExtension.class)
class GroupMemberServiceTest {

    @Mock 
    private GroupMemberRepository groupMemberRepository;
    
    @Mock 
    private GroupMemberQueryProcessor groupMemberQueryProcessor;
    
    @Mock 
    private GroupRepository groupRepository;
    
    @Mock 
    private ApplicationEventPublisher eventPublisher;
    
    @Mock 
    private GroupMemberEventResolver groupMemberEventResolver;
    
    @Mock 
    private GroupService groupService;

    @InjectMocks
    private GroupMemberService groupMemberService;

    @Test
    void inviteUser_whenUserNotExists_shouldCreateInvitation() {

        Group group = Group.builder()
                .groupStatisticsVO(
                        GroupStatisticsVO.builder()
                                .pendingJoinRequests(0)
                                .build()
                )
                .build();

        when(groupService.getActiveGroupOrThrow("group-1")).thenReturn(group);
        when(groupMemberQueryProcessor.getByUserIdAndGroupId(any(), any()))
                .thenReturn(Optional.empty());

        GroupInviteRequest request = GroupInviteRequest.builder()
                .targetUserId("user-1")
                .daysToExpire(5)
                .inviteMessage("Join us")
                .build();

        CustomPrincipal principal = CustomPrincipal.builder()
                .userId("current-user")
                .build();

        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {

            securityUtil.when(SecurityUtil::getCurrentUser)
                    .thenReturn(principal);

            GroupMembershipResponse response =
                    groupMemberService.inviteUser("group-1", request);

            assertNotNull(response);
        }

        verify(groupMemberRepository).save(any(GroupMember.class));
        verify(groupRepository).save(group);
        verify(eventPublisher).publishEvent(any(GroupMemberLog.class));
    }

    @Test
    void inviteUser_whenUserAlreadyInvited_shouldThrowException() {

    	Group group = Group.builder()
    			.groupStatisticsVO(
    					GroupStatisticsVO.builder()
    					.pendingJoinRequests(0)
    					.build()
    					).build();

        when(groupService.getActiveGroupOrThrow("group-1")).thenReturn(group);

        GroupMember existing = GroupMember.builder()
                .membershipStatus(MembershipStatus.PENDING)
                .user(UserVO.builder().id("user-1").build())
                .group(GroupVO.builder().id("group-1").build())
                .build();

        when(groupMemberQueryProcessor.getByUserIdAndGroupId("user-1", "group-1"))
                .thenReturn(Optional.of(existing));

        GroupInviteRequest request = GroupInviteRequest.builder()
                .targetUserId("user-1")
                .build();

        CustomPrincipal principal = CustomPrincipal.builder()
                .userId("current-user")
                .build();

        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {

            securityUtil.when(SecurityUtil::getCurrentUser)
                    .thenReturn(principal);

            assertThrows(
                    InvalidActionException.class,
                    () -> groupMemberService.inviteUser("group-1", request)
            );
        }

        verify(groupMemberRepository, never()).save(any());
        verify(groupRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
