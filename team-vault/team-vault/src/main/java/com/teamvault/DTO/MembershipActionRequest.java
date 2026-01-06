package com.teamvault.DTO;

import com.teamvault.enums.GroupMemberEventType;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MembershipActionRequest {

    @NotNull(message = "Group member event type is required")
    private GroupMemberEventType groupMemberEventType;

    private String notes;
}
