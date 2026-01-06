package com.teamvault.groupmember;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.teamvault.DTO.MembershipActionRequest;
import com.teamvault.DTO.MembershipActionResponse;
import com.teamvault.entity.Group;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.log.GroupMemberLog;
import com.teamvault.enums.GroupMemberEventType;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.event.model.MemberRejectedInvite;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.repository.GroupRepository;
import com.teamvault.service.GroupMemberDomainService;
import com.teamvault.service.GroupService;
import com.teamvault.valueobject.GroupMembershipVO;
import com.teamvault.valueobject.GroupStatisticsVO;
import com.teamvault.valueobject.GroupVO;
import com.teamvault.valueobject.UserVO;

@ExtendWith(MockitoExtension.class)
class MemberRejectedInviteTest {

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private GroupMemberDomainService groupMemberDomainService;

    @Mock
    private GroupService groupService;

    @InjectMocks
    private MemberRejectedInvite memberRejectedInvite;

    @Test
    void applyMembershipAction_shouldRejectInviteAndPublishEvent() {

        GroupStatisticsVO stats = GroupStatisticsVO.builder()
                .pendingJoinRequests(2)
                .build();

        Group group = Group.builder()
                .groupStatisticsVO(stats)
                .build();

        UserVO user = UserVO.builder()
                .id("user-1")
                .build();

        GroupVO groupVO = GroupVO.builder()
                .id("group-1")
                .build();

        GroupMembershipVO membershipVO = GroupMembershipVO.builder()
                .latestMessage("invite-msg")
                .build();

        GroupMember before = GroupMember.builder()
                .id("gm-1")
                .user(user)
                .group(groupVO)
                .membershipStatus(MembershipStatus.PENDING)
                .groupMembershipVO(membershipVO)
                .build();

        when(groupService.getActiveGroupOrThrow("gm-1")).thenReturn(group);
        when(groupMemberDomainService.getInitialGroupMember("gm-1")).thenReturn(before);

        MembershipActionRequest request = MembershipActionRequest.builder()
                .groupMemberEventType(GroupMemberEventType.INVITE_REJECTED)
                .build();

        MembershipActionResponse response =
                memberRejectedInvite.applyMembershipAction("gm-1", request);

        ArgumentCaptor<GroupMemberLog> logCaptor =
                ArgumentCaptor.forClass(GroupMemberLog.class);

        ArgumentCaptor<GroupMember> memberCaptor =
                ArgumentCaptor.forClass(GroupMember.class);

        verify(eventPublisher).publishEvent(logCaptor.capture());
        verify(groupMemberRepository).save(memberCaptor.capture());
        verify(groupRepository).save(group);

        GroupMember saved = memberCaptor.getValue();
        GroupMemberLog log = logCaptor.getValue();

        assertEquals(MembershipStatus.REJECTED, saved.getMembershipStatus());
        assertEquals(GroupMemberEventType.INVITE_REJECTED, log.getEvent());
        assertEquals(MembershipStatus.PENDING, log.getFromStatus());
        assertEquals(MembershipStatus.REJECTED, log.getToStatus());

        assertNotNull(response);
        assertEquals(1, group.getGroupStatisticsVO().getPendingJoinRequests());
    }
}
