package com.h123.waddle.mappers;

import com.h123.waddle.dtos.RequestOutDto;
import com.h123.waddle.entities.Request;

public class RequestMapper {
    public static RequestOutDto requestToReqOutDto(Request r)
    {
        return new RequestOutDto(r.getId(),r.getSender().getName(),r.getReceiver().getName() , r.getState().toString());
    }
}
