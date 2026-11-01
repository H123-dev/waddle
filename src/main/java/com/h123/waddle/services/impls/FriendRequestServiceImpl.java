package com.h123.waddle.services.impls;

import com.h123.waddle.dtos.RequestAnsDto;
import com.h123.waddle.dtos.RequestInDto;
import com.h123.waddle.dtos.RequestOutDto;
import com.h123.waddle.dtos.UserDto;
import com.h123.waddle.entities.FriendRequest;
import com.h123.waddle.entities.Request;
import com.h123.waddle.entities.User;
import com.h123.waddle.entities.enums.ReqState;
import com.h123.waddle.exceptions.ForbiddenRequestException;
import com.h123.waddle.exceptions.InvalidCredentialsException;
import com.h123.waddle.exceptions.ResourceConflictException;
import com.h123.waddle.exceptions.ResourceNotFoundException;
import com.h123.waddle.mappers.RequestMapper;
import com.h123.waddle.mappers.UserMapper;
import com.h123.waddle.repos.FriendRequestRepo;
import com.h123.waddle.repos.UserRepo;
import com.h123.waddle.services.interfaces.RequestService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
        if(friendRequestRepo.isIn(senderId,receiver.getId()).isPresent())
            throw new ResourceConflictException("a request of this kind has already been sent to this user or have been sent from that user to you");
        FriendRequest req = new FriendRequest(sender,receiver);
        friendRequestRepo.save(req);
        return RequestMapper.requestToReqOutDto(req);
    }

    @Override
    public RequestOutDto answerRequest(Long receiver , RequestAnsDto requestAnsDto) throws ResourceNotFoundException , ResourceConflictException , ForbiddenRequestException {
        Optional<FriendRequest> maybe = friendRequestRepo.findById(requestAnsDto.id());
        if(maybe.isEmpty())
            throw new ResourceNotFoundException("request not found");
        FriendRequest req = maybe.get();
        if(!req.getReceiver().getId().equals(receiver))
            throw new ForbiddenRequestException("not yours");
        if(req.getState() != ReqState.Pending)
            throw new ResourceConflictException("request is already answered for");
        ReqState ans = ReqState.valueOf(requestAnsDto.ans());
        req.setState(ans);
        req.setAnswerTime(Instant.now());
        friendRequestRepo.save(req);
        return RequestMapper.requestToReqOutDto(req);
    }

    @Override
    public List<RequestOutDto> getAllIncoming(Long userId) {
        List<FriendRequest> reqs = friendRequestRepo.getIncoming(userId);
        return reqs.stream()
                .map(req -> RequestMapper.requestToReqOutDto(req))
                .toList();
    }

    @Override
    public List<RequestOutDto> getAllOutgoing(Long userId) {
        List<FriendRequest> reqs = friendRequestRepo.getOutgoing(userId);
        return reqs.stream()
                .map(req -> RequestMapper.requestToReqOutDto(req))
                .toList();

    }

    @Override
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void deleteOldRejected() {
        friendRequestRepo.delAllRejected(Instant.now().minus(2,ChronoUnit.HOURS));
    }

    public List<UserDto> getFriends (Long userId)
    {
        List<UserDto> r = new ArrayList<>();
        List<FriendRequest> reqs =friendRequestRepo.getFriends(userId);
        reqs.forEach(
                req -> {
                    if(!req.getReceiver().getId().equals(userId))
                        r.add(UserMapper.userToUserDto(req.getReceiver()));
                    else
                        r.add(UserMapper.userToUserDto(req.getSender()));
                }
        );
        return r;
    }


}
