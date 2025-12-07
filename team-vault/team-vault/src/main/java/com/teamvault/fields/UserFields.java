package com.teamvault.fields;

public final class UserFields {

    private UserFields() {}

    public static final String USER_COLLECTION = "user";

    public static final String ID = "_id";
    
    public static final String NAME = "name";

    public static final String LOOKUP_ALIAS = "invitedByUser";

    // ===== DERIVED PATHS =====
    public static final String NAME_PATH = LOOKUP_ALIAS + "." + NAME;
    
    // ===== ALIAS =====
    public static final String LOOKUP_USER_ALIAS = "lookedUpUserValue";
}
