package com.teamvault.event.model;

import com.teamvault.DTO.MembershipActionRequest;
import com.teamvault.DTO.MembershipActionResponse;
import com.teamvault.entity.GroupMemberLog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class GroupMemberEvent {

    public abstract GroupMemberLog getLog();
    
    public abstract MembershipActionResponse applyMembershipAction(String groupId, MembershipActionRequest request);
}