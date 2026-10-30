package com.h123.waddle.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public  PasswordEncoder creatBcrypt(){return new BCryptPasswordEncoder();}

    @Bean
    public SecurityFilterChain createFilters(HttpSecurity httpSecurity , JwtUtil jwtUtil) throws Exception
    {
        try {
            return httpSecurity
                    .csrf(s-> s.disable())
                    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(a -> a.requestMatchers("/waddle/auth/**").permitAll().anyRequest().authenticated())
                    .addFilterBefore(new TokenFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                    .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public AuthenticationManager getAuthMangaer(AuthenticationConfiguration config) throws Exception
    {
        try {
            return config.getAuthenticationManager();
        }
        catch (Exception e)
        {
            throw new Exception(e);
        }
    }
}
