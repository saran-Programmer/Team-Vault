package com.teamvault.enums;

import java.util.Set;

public enum UserGroupPermission {

    READ_RESOURCE,
    WRITE_RESOURCE,
    PROMOTE_USER,
    DEPROMOTE_USER,
    REMOVE_USER,
    INVITE_USER;

    public static Set<UserGroupPermission> adminPermissions() {
    	
        return Set.of(
            READ_RESOURCE,
            WRITE_RESOURCE,
            PROMOTE_USER,
            DEPROMOTE_USER,
            REMOVE_USER,
            INVITE_USER
        );    
    }
    
    public static Set<UserGroupPermission> minimalPermissions() {
    	
        return Set.of(READ_RESOURCE);
    }

}
