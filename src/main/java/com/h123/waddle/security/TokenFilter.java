package com.h123.waddle.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class TokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public TokenFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if(token==null||!token.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Long id = jwtUtil.getUserIdFromToken(token.substring(7));
            UsernamePasswordAuthenticationToken stamp =
                    new UsernamePasswordAuthenticationToken(id, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(stamp);
        } catch (JwtException | IllegalArgumentException e) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
