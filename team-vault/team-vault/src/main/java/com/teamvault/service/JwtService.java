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
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final Key key;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Autowired
    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expirationTime) {
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(decodedKey);
        this.expirationTime = expirationTime;
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getUserRole().name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getCredentials().getUserName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
    	
        return getClaims(token).getSubject();
    }

    public String extractUserId(String token) {
    	
        return getClaims(token).get("userId", String.class);
    }

    public String extractRole(String token) {
    	
        return getClaims(token).get("role", String.class);
    }

    private Claims getClaims(String token) {
    	
        try {
        	
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
        } catch (SignatureException e) {
        	
            throw new TokenException("Invalid token signature", e);
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

    public void assertValid(String token, UserDetails userDetails) {
    	
        try {
        	
            Claims claims = getClaims(token); 

            if (claims.getExpiration().before(new Date())) {
            	
                throw new TokenException("Token expired");
            }

            String username = claims.getSubject();
            
            if (!username.equals(userDetails.getUsername())) {
            	
                throw new TokenException("Invalid token: username mismatch");
            }

        } catch (ExpiredJwtException e) {
        	
            throw new TokenException("Token expired", e);

        } catch (JwtException e) {
        	
            throw new TokenException("Invalid token", e);
        }
    }


}
