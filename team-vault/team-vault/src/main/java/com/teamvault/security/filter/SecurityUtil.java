package com.teamvault.security.filter;

import com.teamvault.enums.UserRole;
import com.teamvault.models.CustomPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static CustomPrincipal getCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomPrincipal)) {
            return null;
        }

        return (CustomPrincipal) auth.getPrincipal();
    }

    public static String getCurrentUserId() {
    	
        CustomPrincipal user = getCurrentUser();
        
        return user != null ? user.getUserId() : null;
    }

    public boolean hasAuthority(String authority) {
    	
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null) return false;
        
        return auth.getAuthorities().stream()
                   .map(GrantedAuthority::getAuthority)
                   .anyMatch(a -> a.equals(authority));
    }
    
    public static UserRole getCurrentUserRole() {

        CustomPrincipal principal = getCurrentUser();
        if (principal == null || principal.getRole() == null) {
            return null; // or throw SecurityException
        }

        String role = principal.getRole();

        String roleName = role.startsWith("ROLE_")
                ? role.substring(5)
                : role;

        return UserRole.valueOf(roleName);
    }

}
