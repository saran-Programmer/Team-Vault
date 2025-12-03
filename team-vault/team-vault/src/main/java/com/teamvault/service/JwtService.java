package com.teamvault.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.teamvault.entity.User;
import com.teamvault.exception.TokenException;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final Key key;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Autowired
    public JwtService(@Value("${jwt.secret}") String secret,  @Value("${jwt.expiration}") long expirationTime) {
        
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        
        this.key = Keys.hmacShaKeyFor(decodedKey);
        this.expirationTime = expirationTime;
        
    }

    public String generateToken(User user) {
        
        String token = Jwts.builder()
                .setSubject(user.getCredentials().getUserName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        
        return token;
    }

    public String extractUsername(String token) {
        
        try {
            String username = getClaims(token).getSubject();
            return username;
        } catch (Exception e) {
            throw e;
        }
    }

    private Claims getClaims(String token) {
        
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims;
            
        } catch (SignatureException e) {
        	
            throw new TokenException("Invalid token signature - token may have been tampered with", e);
        } catch (ExpiredJwtException e) {

        	throw new TokenException("Token expired", e);
        } catch (MalformedJwtException e) {

        	throw new TokenException("Malformed token", e);
        } catch (UnsupportedJwtException e) {

        	throw new TokenException("Unsupported token", e);
        } catch (IllegalArgumentException e) {

        	throw new TokenException("Token missing", e);
        }
    }

    public void validateToken(String token) {
        getClaims(token);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
    	
        try {
        	
            Claims claims = getClaims(token);
            return claims.getSubject().equals(userDetails.getUsername());
            
        } catch (Exception e) {
        	
            return false;
            
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
        	
            Claims claims = getClaims(token);
            
            return claims.getSubject().equals(userDetails.getUsername()) && !claims.getExpiration().before(new Date());
            
        } catch (Exception e) {
        	
            return false;
        }
    }
}