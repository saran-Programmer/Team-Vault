package com.teamvault.security.filter;

import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.teamvault.entity.User;
import com.teamvault.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        User user = userRepository.findFirstByCredentials_UserNameOrCredentials_Email(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username/email: " + usernameOrEmail));

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getCredentials().getUserName(),
                user.getCredentials().getPassword(),
                Collections.singleton(authority)
        );
    }
}
