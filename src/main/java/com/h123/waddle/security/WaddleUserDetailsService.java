package com.h123.waddle.security;

import com.h123.waddle.entities.User;
import com.h123.waddle.exceptions.InvalidCredentialsException;
import com.h123.waddle.repos.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WaddleUserDetailsService implements UserDetailsService {

    private final UserRepo userRepo;

    public WaddleUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> u  = userRepo.findByEmail(email);
        if(u.isEmpty())
            throw new UsernameNotFoundException("not found");
        User uf = u.get();
        return org.springframework.security.core.userdetails.User.builder().username(email).password(uf.getPasswordHash()).roles(uf.getRole().toString()).build();
    }
}
