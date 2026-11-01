package com.h123.waddle.controllers;

import com.h123.waddle.dtos.RequestAnsDto;
import com.h123.waddle.dtos.RequestInDto;
import com.h123.waddle.dtos.RequestOutDto;
import com.h123.waddle.dtos.UserDto;
import com.h123.waddle.services.impls.FriendRequestServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/waddle/requests")
public class RequestController {

    private final FriendRequestServiceImpl friendRequestServiceImpl;

    public RequestController(FriendRequestServiceImpl friendRequestServiceImpl) {
        this.friendRequestServiceImpl = friendRequestServiceImpl;
    }


    //friendreq section

    //create friendreq
    @PostMapping("/friendrequest/create")
    public ResponseEntity<RequestOutDto> createFriendRequest(@AuthenticationPrincipal Long userId , @RequestBody @Valid RequestInDto requestInDto)
    {
        RequestOutDto out = friendRequestServiceImpl.sendRequest(userId,requestInDto);
        return new ResponseEntity<> (out, HttpStatus.CREATED);
    }


    //answer friendreq
    @PostMapping("/friendrequest/answer")
    public ResponseEntity<RequestOutDto> answerFriendRequest(@AuthenticationPrincipal Long userId , @RequestBody @Valid RequestAnsDto requestAnsDto)
    {
        RequestOutDto out = friendRequestServiceImpl.answerRequest(userId , requestAnsDto);
        return new ResponseEntity<>(out,HttpStatus.OK);
    }

    //get friend list
    @GetMapping("/friendrequest/friends")
    public ResponseEntity<List<UserDto>> getFriendList(@AuthenticationPrincipal Long userId)
    {
        List<UserDto> friends = friendRequestServiceImpl.getFriends(userId);
        return new ResponseEntity<>(friends,HttpStatus.OK);
    }

    //get Icoming pending friendeqs
    @GetMapping("/friendrequest/incoming")
    public ResponseEntity<List<RequestOutDto>> getIncomingRequests(@AuthenticationPrincipal Long userId)
    {
        List<RequestOutDto> all = friendRequestServiceImpl.getAllIncoming(userId);
        return new ResponseEntity<>(all,HttpStatus.OK);
    }

    //get outgoing pending friendreqs
    @GetMapping("/friendrequest/outgoing")
    public ResponseEntity<List<RequestOutDto>> getOutgoingRequests(@AuthenticationPrincipal Long userId)
    {
        List<RequestOutDto> all = friendRequestServiceImpl.getAllOutgoing(userId);
        return new ResponseEntity<>(all,HttpStatus.OK);
    }


}
