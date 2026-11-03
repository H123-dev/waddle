package com.h123.waddle.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GuessInDto(@NotNull @Size(min = 5 , max = 5) String g , @NotNull Long waddleID )  {
}
