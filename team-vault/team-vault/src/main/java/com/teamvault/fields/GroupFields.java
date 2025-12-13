package com.teamvault.fields;

public final class GroupFields {

    private GroupFields() {}

    public static final String GROUP_COLLECTION = "group";

    /* ========= ROOT ========= */

    public static final String ID = "_id";
    
    public static final String GROUP_DETAILS = "groupDetailsVO";
    
    public static final String GROUP_TITLE = GROUP_DETAILS + ".title";
    
    public static final String GROUP_VISIBLITY = "groupVisibility";

    /* ========= EMBEDDED ========= */

    public static final String GROUP_ID_PATH = "group._id";

    /* ========= LOOKUP ========= */

    public static final String LOOKUP_ALIAS = "invitedGroup";

    /* ========= DERIVED ========= */

    public static final String GROUP_DETAILS_DERIVED = LOOKUP_ALIAS + "." + GROUP_DETAILS;
    
    // ===== ALIAS =====
    public static final String LOOKUP_GROUP_ALIAS = "lookedUpGroupValue";
}
