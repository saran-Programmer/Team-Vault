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
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.log.GroupMemberLog;
import com.teamvault.enums.GroupMemberEventType;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.event.model.MemberAcceptedInvite;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.service.GroupMemberDomainService;
import com.teamvault.valueobject.GroupMembershipVO;
import com.teamvault.valueobject.GroupVO;
import com.teamvault.valueobject.UserVO;

@ExtendWith(MockitoExtension.class)
class MemberAcceptedInviteTest {

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private GroupMemberDomainService groupMemberDomainService;

    @InjectMocks
    private MemberAcceptedInvite memberAcceptedInvite;

    @Test
    void applyMembershipAction_shouldAcceptInviteAndPublishEvent() {

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

        when(groupMemberDomainService.getInitialGroupMember("gm-1")).thenReturn(before);

        MembershipActionRequest request = MembershipActionRequest.builder()
                .groupMemberEventType(GroupMemberEventType.INVITE_ACCEPTED)
                .build();

        MembershipActionResponse response =
                memberAcceptedInvite.applyMembershipAction("gm-1", request);

        ArgumentCaptor<GroupMember> memberCaptor =
                ArgumentCaptor.forClass(GroupMember.class);

        ArgumentCaptor<GroupMemberLog> logCaptor =
                ArgumentCaptor.forClass(GroupMemberLog.class);

        verify(eventPublisher).publishEvent(logCaptor.capture());
        verify(groupMemberRepository).save(memberCaptor.capture());

        GroupMember saved = memberCaptor.getValue();
        GroupMemberLog log = logCaptor.getValue();

        assertEquals(MembershipStatus.ACTIVE, saved.getMembershipStatus());
        assertEquals(UserGroupPermission.minimalPermissions(), saved.getUserPermissions());

        assertEquals(GroupMemberEventType.INVITE_ACCEPTED, log.getEvent());
        assertEquals(MembershipStatus.PENDING, log.getFromStatus());
        assertEquals(MembershipStatus.ACTIVE, log.getToStatus());

        assertNotNull(response);
    }
}
