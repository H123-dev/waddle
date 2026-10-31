package com.h123.waddle.services.impls;

import com.h123.waddle.dtos.RequestInDto;
import com.h123.waddle.dtos.RequestOutDto;
import com.h123.waddle.entities.FriendRequest;
import com.h123.waddle.entities.User;
import com.h123.waddle.exceptions.ResourceConflictException;
import com.h123.waddle.exceptions.ResourceNotFoundException;
import com.h123.waddle.mappers.RequestMapper;
import com.h123.waddle.repos.FriendRequestRepo;
import com.h123.waddle.repos.UserRepo;
import com.h123.waddle.services.interfaces.RequestService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FriendRequestServiceImpl implements RequestService {

    private final UserRepo userRepo;
    private final FriendRequestRepo friendRequestRepo;

    public FriendRequestServiceImpl(UserRepo userRepo, FriendRequestRepo friendRequestRepo) {
        this.userRepo = userRepo;
        this.friendRequestRepo = friendRequestRepo;
    }


    @Override
    public RequestOutDto sendRequest(Long senderId, RequestInDto requestInDto) throws ResourceNotFoundException, ResourceConflictException
    {
        Optional<User> receiverMaybe =  userRepo.findByName(requestInDto.name());
        if(receiverMaybe.isEmpty())
            throw new ResourceNotFoundException("user not found");
        User receiver = receiverMaybe.get();
        if(senderId.equals(receiver.getId()))
            throw new ResourceConflictException("cant befriend yourself");
        User sender = userRepo.findById(senderId).get();
        if(friendRequestRepo.isIn(senderId,receiver.getId()))
            throw new ResourceConflictException("a request of this kind has already been sent to this user or have been sent from that use to you");
        FriendRequest req = new FriendRequest(sender,receiver);
        friendRequestRepo.save(req);
        return RequestMapper.requestToReqOutDto(req);
    }
}
