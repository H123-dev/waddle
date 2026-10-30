package com.h123.waddle.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserSignUpDto(
        @NotBlank
        @Size(max = 256)
        @Email
        String email ,
        @NotBlank
        @Size(max = 256)
        String name ,
        @NotBlank
        @Size(min = 8 , max =72)
        String password
)
{ }
