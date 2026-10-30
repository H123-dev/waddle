package com.h123.waddle.security;

import com.h123.waddle.exceptions.InvalidCredentialsException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil
{
    private int expInMs;
    private final SecretKey key;

    public JwtUtil(@Value("${jwt.secret}") String jwtSecret ,@Value("${jwt.expiration}") int expInMs) {
        this.expInMs = expInMs;
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long id)
    {
        return Jwts.builder()
                .subject(String.valueOf(id))
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime()+this.expInMs))
                .signWith(key)
                .compact();
    }

    public Long getUserIdFromToken (String token) throws JwtException
    {
        return Long.valueOf(Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject());
    }
}
