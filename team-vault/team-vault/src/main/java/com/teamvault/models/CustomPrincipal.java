package com.teamvault.models;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomPrincipal {

    private String userId;
    
    private String username;
    
    private String role;
    
    private Collection<? extends GrantedAuthority> authorities;
}
