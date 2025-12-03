package com.teamvault.security.filter;

import com.teamvault.models.CustomPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static CustomPrincipal getCurrentUser() {
    	
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !(auth.getPrincipal() instanceof CustomPrincipal)) return null;
        
        return (CustomPrincipal) auth.getPrincipal();
    }
}