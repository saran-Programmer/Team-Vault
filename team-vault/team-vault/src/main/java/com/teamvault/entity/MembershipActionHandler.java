package com.teamvault.entity;

import com.teamvault.DTO.MembershipActionRequest;
import com.teamvault.DTO.MembershipActionResponse;

public interface MembershipActionHandler {

	public MembershipActionResponse handle(GroupMember groupMember, MembershipActionRequest request);
}
