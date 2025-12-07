package com.teamvault.DTO;

import java.time.Instant;

import com.teamvault.enums.MembershipStatus;
import com.teamvault.valueobject.GroupDetailsVO;
import com.teamvault.valueobject.NameVO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupInviteResponse {
	
	private String id;
	
	private String groupId;
	
	private GroupDetailsVO groupDetailsVO;

    private String invitedUserId;
    
    private NameVO invitedUserName;
    
    private String targetUserId;
    
    private MembershipStatus status;
    
    private Instant invitedAt;
    
    private String message;
    
    private Instant expiresAt;
}
