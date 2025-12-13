package com.teamvault.event.model;

import com.teamvault.DTO.MembershipActionRequest;
import com.teamvault.DTO.MembershipActionResponse;
import com.teamvault.entity.Group;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.GroupMemberLog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class GroupMemberEvent {

    protected abstract GroupMemberLog getLog(GroupMember beforeUpdate, GroupMember afterUpdate);
    
    protected abstract void updateGroupStatistics(Group group);
    
    public abstract MembershipActionResponse applyMembershipAction(String groupId, MembershipActionRequest request);
}