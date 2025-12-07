package com.teamvault.DTO;

import com.teamvault.enums.GroupMemberEventType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MembershipActionRequest {

    @NotNull(message = "Group member event type is required")
    private GroupMemberEventType groupMemberEventType;

    private String notes;
}
