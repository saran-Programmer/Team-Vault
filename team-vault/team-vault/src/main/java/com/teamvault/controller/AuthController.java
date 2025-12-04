package com.teamvault.controller;

import com.teamvault.DTO.SignUpRequest;
import com.teamvault.DTO.UserPatchRequest;
import com.teamvault.models.CustomPrincipal;
import com.teamvault.DTO.LoginRequest;
import com.teamvault.DTO.AuthResponse;
import com.teamvault.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody @Valid SignUpRequest request) {
    	
        AuthResponse response = authService.signup(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
    	
        AuthResponse response = authService.login(request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<CustomPrincipal> getCurrentUser() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomPrincipal)) {
        	
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        
        return ResponseEntity.ok(principal);
    }    
}
