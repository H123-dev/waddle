package com.h123.waddle.controllers;

import com.h123.waddle.dtos.RequestInDto;
import com.h123.waddle.dtos.RequestOutDto;
import com.h123.waddle.services.impls.FriendRequestServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/waddle/requests")
public class RequestController {

    private final FriendRequestServiceImpl friendRequestServiceImpl;

    public RequestController(FriendRequestServiceImpl friendRequestServiceImpl) {
        this.friendRequestServiceImpl = friendRequestServiceImpl;
    }

    //create friendreq
    @PostMapping("/friendrequest")
    public ResponseEntity<RequestOutDto> createFriendRequest(@AuthenticationPrincipal Long userId , @RequestBody @Valid RequestInDto requestInDto)
    {
        RequestOutDto out = friendRequestServiceImpl.sendRequest(userId,requestInDto);
        return new ResponseEntity<> (out, HttpStatus.CREATED);
    }

}
