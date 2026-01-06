package com.teamvault.groupmember;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
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
import com.teamvault.event.model.MemberExitedGroup;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.repository.GroupRepository;
import com.teamvault.service.GroupMemberDomainService;
import com.teamvault.service.GroupService;
import com.teamvault.valueobject.GroupMembershipVO;
import com.teamvault.valueobject.GroupStatisticsVO;
import com.teamvault.valueobject.GroupVO;
import com.teamvault.valueobject.UserVO;

@ExtendWith(MockitoExtension.class)
class MemberExitedGroupTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private GroupMemberDomainService groupMemberDomainService;

    @Mock
    private GroupService groupService;

    @InjectMocks
    private MemberExitedGroup memberExitedGroup;

    @Test
    void applyMembershipAction_shouldExitMemberAndPublishEvent() {

        GroupStatisticsVO statisticsVO = GroupStatisticsVO.builder()
                .members(3)
                .build();

        Group group = Group.builder()
                .id("group-1")
                .groupStatisticsVO(statisticsVO)
                .build();

        UserVO user = UserVO.builder()
                .id("user-1")
                .build();

        GroupMembershipVO membershipVO = GroupMembershipVO.builder()
                .latestMessage("exit-msg")
                .build();

        GroupMember beforeUpdate = GroupMember.builder()
                .id("gm-1")
                .user(user)
                .group(GroupVO.builder().id("group-1").build())
                .membershipStatus(MembershipStatus.ACTIVE)
                .groupMembershipVO(membershipVO)
                .userPermissions(Set.of())
                .build();

        when(groupService.getActiveGroupOrThrow("group-1")).thenReturn(group);
        when(groupMemberDomainService.getInitialGroupMember("group-1")).thenReturn(beforeUpdate);

        MembershipActionRequest request = MembershipActionRequest.builder()
                .groupMemberEventType(GroupMemberEventType.MEMBER_EXITED)
                .build();

        MembershipActionResponse response =
                memberExitedGroup.applyMembershipAction("group-1", request);

        assertEquals(MembershipStatus.EXITED, response.getMembershipStatus());
        assertEquals(4, group.getGroupStatisticsVO().getMembers());

        verify(eventPublisher).publishEvent(ArgumentMatchers.any(GroupMemberLog.class));
        verify(groupMemberRepository).save(ArgumentMatchers.any(GroupMember.class));
        verify(groupRepository).save(group);
    }
}
