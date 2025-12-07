package com.teamvault.fields;

public final class GroupMemberFields {

    private GroupMemberFields() {}

    /* ========= Collection ========= */

    public static final String GROUP_MEMBER_COLLECTION = "group_member";

    /* ========= RAW MONGO FIELDS ========= */

    public static final String ID = "id";
    
    public static final String MEMBERSHIP_STATUS = "membershipStatus";
    
    public static final String CREATED_DATE = "createdDate";
    
    public static final String EXPIRES_AT = "expiresAt";

    public static final String USER_ID = "user._id";
    public static final String GROUP_ID = "group._id";

    public static final String INVITED_BY_USER_ID = "groupMembershipVO.invitedByUser._id";

    public static final String INVITE_MESSAGE = "groupMembershipVO.inviteMessage";

    /* ========= LOOKUP ALIASES ========= */

    public static final String INVITED_USER_ALIAS = "invitedByUser";
    
    public static final String INVITED_GROUP_ALIAS = "invitedGroup";

    /* ========= DERIVED (SAFE CONCATENATIONS) ========= */

    public static final String GROUP_DETAILS_DERIVED = INVITED_GROUP_ALIAS + ".groupDetailsVO";

    public static final String INVITED_USER_NAME_DERIVED = INVITED_USER_ALIAS + "." + UserFields.NAME;

    public static final String INVITED_USER_ID_DERIVED = INVITED_USER_ALIAS + "." + UserFields.ID;
    
}
