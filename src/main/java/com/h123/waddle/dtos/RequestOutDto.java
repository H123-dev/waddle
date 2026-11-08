package com.h123.waddle.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)

public record RequestOutDto(Long id , String sender , String receiver , String reqState , Optional<Long> wordleID) {

}
