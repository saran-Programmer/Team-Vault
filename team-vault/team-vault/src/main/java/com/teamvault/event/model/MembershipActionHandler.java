package com.teamvault.event.model;

import com.teamvault.DTO.MembershipActionRequest;
import com.teamvault.DTO.MembershipActionResponse;
import com.teamvault.entity.GroupMember;

public interface MembershipActionHandler {

	public MembershipActionResponse handle(GroupMember groupMember, MembershipActionRequest request);
}
