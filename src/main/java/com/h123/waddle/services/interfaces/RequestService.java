package com.h123.waddle.services.interfaces;

import com.h123.waddle.dtos.RequestInDto;
import com.h123.waddle.dtos.RequestOutDto;

public interface RequestService {
    public RequestOutDto sendRequest(Long senderId , RequestInDto requestInDto);
}
