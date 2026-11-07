package com.h123.waddle.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WordDto(@NotNull @Size(min = 5 , max = 5 )   @Pattern(regexp = "^[A-Z]+$", message = "The field must contain only uppercase letters") String word) {
}
