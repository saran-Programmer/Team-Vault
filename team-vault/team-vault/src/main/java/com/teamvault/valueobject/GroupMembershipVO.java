package com.teamvault.valueobject;

import java.time.Instant;

import com.teamvault.enums.MembershipStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class GroupMembershipVO {

    private UserVO invitedByUser;
    
    private Instant invitationSentAt;
    
    private boolean isFirstAdmin;

    private Instant joinedAt;
    
    private String inviteMessage;
}