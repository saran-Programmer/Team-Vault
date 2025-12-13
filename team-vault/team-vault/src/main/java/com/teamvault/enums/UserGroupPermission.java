package com.teamvault.enums;

import java.util.Set;

public enum UserGroupPermission {

    READ_RESOURCE,
    WRITE_RESOURCE,
    MANAGE_USER_ROLES,
    REMOVE_USER,
    INVITE_USER;

    public static Set<UserGroupPermission> adminPermissions() {
    	
        return Set.of(
            READ_RESOURCE,
            WRITE_RESOURCE,
            MANAGE_USER_ROLES,
            REMOVE_USER,
            INVITE_USER
        );    
    }
    
    public static Set<UserGroupPermission> minimalPermissions() {
    	
        return Set.of(READ_RESOURCE);
    }

}
