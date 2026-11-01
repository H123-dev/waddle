package com.h123.waddle.services.interfaces;

import com.h123.waddle.dtos.RequestAnsDto;
import com.h123.waddle.dtos.RequestInDto;
import com.h123.waddle.dtos.RequestOutDto;

import java.util.List;

public interface RequestService {
    public RequestOutDto sendRequest(Long senderId , RequestInDto requestInDto);
    public RequestOutDto answerRequest(Long receiver , RequestAnsDto requestAnsDto);
    public List<RequestOutDto> getAllIncoming(Long userId);
    public List<RequestOutDto> getAllOutgoing(Long userId);

}
