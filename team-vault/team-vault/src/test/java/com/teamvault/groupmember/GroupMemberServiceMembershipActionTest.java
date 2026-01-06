package com.teamvault.groupmember;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.teamvault.DTO.MembershipActionRequest;
import com.teamvault.DTO.MembershipActionResponse;
import com.teamvault.enums.GroupMemberEventType;
import com.teamvault.event.model.GroupMemberEvent;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.service.GroupMemberService;
import com.teamvault.event.resolver.GroupMemberEventResolver;

@ExtendWith(MockitoExtension.class)
class GroupMemberServiceMembershipActionTest {

    @Mock
    private GroupMemberEventResolver groupMemberEventResolver;

    @Mock
    private GroupMemberEvent groupMemberEvent;

    @InjectMocks
    private GroupMemberService groupMemberService;

    @Test
    void performMembershipAction_whenInviteAccepted_shouldDelegateToHandler() {

        MembershipActionRequest request = MembershipActionRequest.builder()
                .groupMemberEventType(GroupMemberEventType.INVITE_ACCEPTED)
                .build();

        MembershipActionResponse expectedResponse =
                MembershipActionResponse.builder().build();

        when(groupMemberEventResolver.resolve(GroupMemberEventType.INVITE_ACCEPTED))
                .thenReturn(groupMemberEvent);

        when(groupMemberEvent.applyMembershipAction("gm-1", request))
                .thenReturn(expectedResponse);

        MembershipActionResponse actual =
                groupMemberService.performMembershipAction("gm-1", request);

        assertEquals(expectedResponse, actual);
        verify(groupMemberEventResolver).resolve(GroupMemberEventType.INVITE_ACCEPTED);
        verify(groupMemberEvent).applyMembershipAction("gm-1", request);
    }

    @Test
    void performMembershipAction_whenInviteRejected_shouldDelegateToHandler() {

        MembershipActionRequest request = MembershipActionRequest.builder()
                .groupMemberEventType(GroupMemberEventType.INVITE_REJECTED)
                .build();

        MembershipActionResponse expectedResponse =
                MembershipActionResponse.builder().build();

        when(groupMemberEventResolver.resolve(GroupMemberEventType.INVITE_REJECTED))
                .thenReturn(groupMemberEvent);

        when(groupMemberEvent.applyMembershipAction("gm-2", request))
                .thenReturn(expectedResponse);

        MembershipActionResponse actual =
                groupMemberService.performMembershipAction("gm-2", request);

        assertEquals(expectedResponse, actual);
        verify(groupMemberEventResolver).resolve(GroupMemberEventType.INVITE_REJECTED);
        verify(groupMemberEvent).applyMembershipAction("gm-2", request);
    }

    @Test
    void performMembershipAction_whenMemberExited_shouldDelegateToHandler() {

        MembershipActionRequest request = MembershipActionRequest.builder()
                .groupMemberEventType(GroupMemberEventType.MEMBER_EXITED)
                .build();

        MembershipActionResponse expectedResponse =
                MembershipActionResponse.builder().build();

        when(groupMemberEventResolver.resolve(GroupMemberEventType.MEMBER_EXITED))
                .thenReturn(groupMemberEvent);

        when(groupMemberEvent.applyMembershipAction("gm-3", request))
                .thenReturn(expectedResponse);

        MembershipActionResponse actual =
                groupMemberService.performMembershipAction("gm-3", request);

        assertEquals(expectedResponse, actual);
        verify(groupMemberEventResolver).resolve(GroupMemberEventType.MEMBER_EXITED);
        verify(groupMemberEvent).applyMembershipAction("gm-3", request);
    }

    @Test
    void performMembershipAction_whenInvalidEvent_shouldThrowException() {

        MembershipActionRequest request = MembershipActionRequest.builder()
                .groupMemberEventType(GroupMemberEventType.MEMBER_REMOVED) 
                .build();

        assertThrows(InvalidActionException.class, () -> groupMemberService.performMembershipAction("gm-4", request));

    }
}
