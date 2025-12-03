package com.teamvault.enums;

public enum UserRole {
	
    USER(1),
    ADMIN(2),
    SUPER_ADMIN(3);

    private final int level;

    UserRole(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
