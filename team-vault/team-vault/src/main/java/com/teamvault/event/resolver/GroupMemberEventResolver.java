package com.teamvault.event.resolver;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.teamvault.enums.GroupMemberEventType;
import com.teamvault.event.model.GroupMemberEvent;
import com.teamvault.event.model.MemberAcceptedInvite;
import com.teamvault.event.model.MemberExitedGroup;
import com.teamvault.event.model.MemberRejectedInvite;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GroupMemberEventResolver {

    private final MemberAcceptedInvite memberAcceptedInvite;
    
    private final MemberRejectedInvite memberRejectedInvite;
    
    private final MemberExitedGroup memberExitedGroup;

    private final Map<GroupMemberEventType, GroupMemberEvent> registry =
        new EnumMap<>(GroupMemberEventType.class);

    @PostConstruct
    private void init() {
        registry.put(GroupMemberEventType.INVITE_ACCEPTED, memberAcceptedInvite);
        registry.put(GroupMemberEventType.INVITE_REJECTED, memberRejectedInvite);
        registry.put(GroupMemberEventType.MEMBER_EXITED, memberExitedGroup);
    }

    public GroupMemberEvent resolve(GroupMemberEventType eventType) {

        return registry.get(eventType);
    }
}
