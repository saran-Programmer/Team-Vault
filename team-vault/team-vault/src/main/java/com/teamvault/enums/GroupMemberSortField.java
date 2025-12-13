package com.teamvault.enums;

import com.teamvault.fields.GroupFields;
import com.teamvault.fields.GroupMemberFields;

public enum GroupMemberSortField {

    LAST_ACCESSED(GroupMemberFields.LAST_ACCESSED),
    LAST_MODIFIED(GroupMemberFields.LAST_WRITE),
    GROUP_TITLE(GroupFields.GROUP_TITLE);
	
	public static final String DEFAULT = "LAST_ACCESSED";

    private final String field;

    GroupMemberSortField(String field) {
        this.field = field;
    }

    public String getField() {
    	
        return field;
    }
}
