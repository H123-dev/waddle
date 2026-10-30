package com.h123.waddle.services.impls;

import com.h123.waddle.dtos.UserDto;
import com.h123.waddle.dtos.UserInitTokenDto;
import com.h123.waddle.dtos.UserSignInDto;
import com.h123.waddle.dtos.UserSignUpDto;
import com.h123.waddle.entities.User;
import com.h123.waddle.exceptions.InvalidCredentialsException;
import com.h123.waddle.exceptions.ResourceConflictException;
import com.h123.waddle.mappers.UserMapper;
import com.h123.waddle.repos.UserRepo;
import com.h123.waddle.security.JwtUtil;
import com.h123.waddle.services.interfaces.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder encoder;
    private final UserRepo userRepo;
    private final AuthenticationManager authmanger;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(PasswordEncoder encoder, UserRepo userRepo, AuthenticationManager authmanger, JwtUtil jwtUtil) {
        this.encoder = encoder;
        this.userRepo = userRepo;
        this.authmanger = authmanger;
        this.jwtUtil = jwtUtil;
    }


    @Override
    public UserDto signUp(UserSignUpDto ud) throws ResourceConflictException {
        if(userRepo.existsByEmail(ud.email())||userRepo.existsByName(ud.name()))
            throw new ResourceConflictException("User with this name or email already exists");
        User u = UserMapper.userSignUpToUser(ud);
        u.setPasswordHash(encoder.encode(u.getPasswordHash()));
        userRepo.save(u);
        return UserMapper.userToUserDto(u);
    }

    @Override
    public UserInitTokenDto signIn(UserSignInDto ud)throws InvalidCredentialsException {
        try {
            authmanger.authenticate(new UsernamePasswordAuthenticationToken(ud.email(), ud.password()));
            User u = userRepo.findByEmail(ud.email()).get();
            String token = jwtUtil.generateToken(u.getId());
            return new UserInitTokenDto(u.getName(),u.getId(),token);
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("bad creds");
        }
    }
}
