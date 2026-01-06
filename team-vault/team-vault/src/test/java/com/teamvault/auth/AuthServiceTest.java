package com.teamvault.auth;

import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.teamvault.DTO.LoginRequest;
import com.teamvault.DTO.SignUpRequest;
import com.teamvault.entity.User;
import com.teamvault.exception.InvalidCredentialsException;
import com.teamvault.exception.UserExistsException;
import com.teamvault.queryprocessor.AuthQueryProcessor;
import com.teamvault.repository.UserRepository;
import com.teamvault.service.AuthService;
import com.teamvault.service.JwtService;
import com.teamvault.valueobject.CredentialsVO;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;
    
    @Mock
    private AuthQueryProcessor authQueryProcessor;
    
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_callsDoesUserExists() {
    	
        SignUpRequest request = new SignUpRequest();
        request.setUsername("newUser");
        request.setPrimaryEmail("new@example.com");
        request.setPassword("password123");

        when(authQueryProcessor.doesUserExists(anyString(), anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("mocked-jwt-token");

        authService.signup(request);

        verify(authQueryProcessor, times(1))
                .doesUserExists(request.getUsername(), request.getPrimaryEmail());
    }

    
    @Test
    public void signup_userAlreadyExists_throwsException() {

        SignUpRequest request = new SignUpRequest();
        request.setUsername("existingUser");
        request.setPrimaryEmail("existing@example.com");
        request.setPassword("password123");

        when(authQueryProcessor.doesUserExists(anyString(), anyString())).thenReturn(true);

        assertThrows(
                UserExistsException.class,
                () -> authService.signup(request)
        );

        verify(authQueryProcessor, times(1))
                .doesUserExists(request.getUsername(), request.getPrimaryEmail());

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }
    
    @Test
    void login_wrongPassword_throwsException() {

        LoginRequest request = LoginRequest.builder()
        		.userName("testuser")
        		.password("wrongPassword").build();

        User user = new User();
        
        CredentialsVO credentialsVO = CredentialsVO.builder().userName("testuser").password("encodedPassword").build();
        user.setCredentials(credentialsVO);

        when(userRepository.findFirstByCredentials_UserNameOrCredentials_Email(
                anyString(), anyString()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(),
                user.getCredentials().getPassword()))
                .thenReturn(false);
        
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(userRepository, times(1))
                .findFirstByCredentials_UserNameOrCredentials_Email(
                        request.getUserName(),
                        request.getUserName()
                );

        verify(passwordEncoder, times(1))
                .matches(anyString(), anyString());

        verify(jwtService, never()).generateToken(any());
        verify(userRepository, never()).save(any());
    }

}
