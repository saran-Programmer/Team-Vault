package com.teamvault.enums;

import java.util.Arrays;
import java.util.List;
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
    
    public static List<String> allPermissionNames() {
        return Arrays.stream(UserGroupPermission.values())
                .map(Enum::name)
                .toList();
    }
}
