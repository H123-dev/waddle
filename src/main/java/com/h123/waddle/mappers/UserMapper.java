package com.h123.waddle.mappers;

import com.h123.waddle.dtos.UserDto;
import com.h123.waddle.dtos.UserSignInDto;
import com.h123.waddle.dtos.UserSignUpDto;
import com.h123.waddle.entities.User;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserMapper {
    public static UserDto userToUserDto(User u)
    {return new UserDto(u.getName(), u.getId()) ;}

    public static User userSignUpToUser(UserSignUpDto ud)
    {
        return new User(ud.name(),ud.email(),ud.password());
    }

}
