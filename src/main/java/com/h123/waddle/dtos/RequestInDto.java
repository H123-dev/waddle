package com.h123.waddle.dtos;

import jakarta.validation.constraints.NotBlank;

public record RequestInDto(@NotBlank String name) {
}
