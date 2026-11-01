package com.h123.waddle.services.impls;

import com.h123.waddle.dtos.RequestAnsDto;
import com.h123.waddle.dtos.RequestInDto;
import com.h123.waddle.dtos.RequestOutDto;
import com.h123.waddle.entities.FriendRequest;
import com.h123.waddle.entities.MatchRequest;
import com.h123.waddle.entities.User;
import com.h123.waddle.entities.enums.ReqState;
import com.h123.waddle.exceptions.ForbiddenRequestException;
import com.h123.waddle.exceptions.ResourceConflictException;
import com.h123.waddle.exceptions.ResourceNotFoundException;
import com.h123.waddle.mappers.RequestMapper;
import com.h123.waddle.repos.FriendRequestRepo;
import com.h123.waddle.repos.MatchRequestRepo;
import com.h123.waddle.repos.UserRepo;
import com.h123.waddle.services.interfaces.RequestService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Service
public class MatchRequestServiceImpl implements RequestService {


    private final MatchRequestRepo matchRequestRepo;
    private final FriendRequestRepo friendRequestRepo;
    private final UserRepo userRepo;

    public MatchRequestServiceImpl(MatchRequestRepo matchRequestRepo, FriendRequestRepo friendRequestRepo, UserRepo userRepo) {
        this.matchRequestRepo = matchRequestRepo;
        this.friendRequestRepo = friendRequestRepo;
        this.userRepo = userRepo;
    }

    @Override
    public RequestOutDto sendRequest(Long senderId, RequestInDto requestInDto) throws ResourceNotFoundException, ResourceConflictException , ForbiddenRequestException
    {
        Optional<User> receiverMaybe =  userRepo.findByName(requestInDto.name());
        if(receiverMaybe.isEmpty())
            throw new ResourceNotFoundException("user not found");
        User receiver = receiverMaybe.get();
        if(senderId.equals(receiver.getId()))
            throw new ResourceConflictException("cant challenge yourself");
        User sender = userRepo.findById(senderId).get();
        Optional<FriendRequest> maybe = friendRequestRepo.isIn(senderId,receiver.getId());
        if(maybe.isEmpty()||maybe.get().getState()!= ReqState.Accepted)
            throw new ForbiddenRequestException("you are not friends with this user");
        if(matchRequestRepo.isIn(senderId,receiver.getId()))
            throw new ResourceConflictException("a request of this kind has already been sent to this user or have been sent from that user to you");
        MatchRequest request = new MatchRequest(sender,receiver);
        matchRequestRepo.save(request);
        return RequestMapper.requestToReqOutDto(request);
    }

    @Override
    public RequestOutDto answerRequest(Long receiver, RequestAnsDto requestAnsDto)throws ResourceNotFoundException , ResourceConflictException , ForbiddenRequestException {
        Optional<MatchRequest> maybe = matchRequestRepo.findById(requestAnsDto.id());
        if(maybe.isEmpty())
            throw new ResourceNotFoundException("match request not found");
        MatchRequest req = maybe.get();
        if(!req.getReceiver().getId().equals(receiver))
            throw new ForbiddenRequestException("not your request");
        if(req.getState()!=ReqState.Pending)
            throw new ResourceConflictException("request is already answered for");
        ReqState state = ReqState.valueOf(requestAnsDto.ans());
        req.setState(state);
        if(state==ReqState.Accepted)
        {
            //todu : make the actual match
        }
        req.setAnswerTime(Instant.now());
        matchRequestRepo.save(req);
        return RequestMapper.requestToReqOutDto(req);
    }

    @Override
    public List<RequestOutDto> getAllIncoming(Long userId) {
        List<MatchRequest> all = matchRequestRepo.getIncoming(userId);
        return all.stream()
                .map(req -> RequestMapper.requestToReqOutDto(req))
                .toList();
    }

    @Override
    public List<RequestOutDto> getAllOutgoing(Long userId) {
        List<MatchRequest> all = matchRequestRepo.getOutgoing(userId);
        return all.stream()
                .map(req -> RequestMapper.requestToReqOutDto(req))
                .toList();
    }

    @Override
    @Scheduled(fixedRate = 1,timeUnit = TimeUnit.HOURS)
    public void deleteOldRejected() {
        matchRequestRepo.delAllRejected(Instant.now().minus(2, ChronoUnit.HOURS));
    }
}
