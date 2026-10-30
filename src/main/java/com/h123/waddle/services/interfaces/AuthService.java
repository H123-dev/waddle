package com.h123.waddle.services.interfaces;

import com.h123.waddle.dtos.UserDto;
import com.h123.waddle.dtos.UserInitTokenDto;
import com.h123.waddle.dtos.UserSignInDto;
import com.h123.waddle.dtos.UserSignUpDto;

public interface AuthService
{
    public UserDto signUp(UserSignUpDto ud);
    public UserInitTokenDto signIn(UserSignInDto ud) ;
}
