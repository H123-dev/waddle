package com.h123.waddle.controllers;

import com.h123.waddle.dtos.UserDto;
import com.h123.waddle.dtos.UserInitTokenDto;
import com.h123.waddle.dtos.UserSignInDto;
import com.h123.waddle.dtos.UserSignUpDto;
import com.h123.waddle.services.impls.AuthServiceImpl;
import com.h123.waddle.services.interfaces.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/waddle/auth")
public class AuthController
{
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    //sign up
    @PostMapping("/signup")
    public ResponseEntity<UserDto> signUp(@RequestBody @Valid UserSignUpDto ud)
    {
        UserDto savedUser = authService.signUp(ud);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    //sign in
    @PostMapping("/signin")
    public ResponseEntity<UserInitTokenDto> signIn(@RequestBody @Valid UserSignInDto ud)
    {
        UserInitTokenDto foundUser = authService.signIn(ud);
        return new ResponseEntity<>(foundUser , HttpStatus.OK);
    }


}
