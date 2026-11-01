package com.h123.waddle.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RequestAnsDto(@NotNull @Pattern(regexp = "Accepted|Rejected", message = "Answer must be either Accepted or Rejected") String ans ,@NotNull Long id) {
}
