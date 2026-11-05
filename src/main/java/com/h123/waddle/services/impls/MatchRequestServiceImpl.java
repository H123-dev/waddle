package com.h123.waddle.services.impls;

import com.h123.waddle.dtos.RequestAnsDto;
import com.h123.waddle.dtos.RequestInDto;
import com.h123.waddle.dtos.RequestOutDto;
import com.h123.waddle.entities.*;
import com.h123.waddle.entities.enums.GameState;
import com.h123.waddle.entities.enums.ReqState;
import com.h123.waddle.exceptions.ForbiddenRequestException;
import com.h123.waddle.exceptions.ResourceConflictException;
import com.h123.waddle.exceptions.ResourceNotFoundException;
import com.h123.waddle.mappers.RequestMapper;
import com.h123.waddle.repos.*;
import com.h123.waddle.services.interfaces.GameService;
import com.h123.waddle.services.interfaces.RequestService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    private final GameService gameService;
    private final WordleRepo wordleRepo;

    public MatchRequestServiceImpl(MatchRequestRepo matchRequestRepo, FriendRequestRepo friendRequestRepo, UserRepo userRepo, GameService gameService, WordleRepo wordleRepo) {
        this.matchRequestRepo = matchRequestRepo;
        this.friendRequestRepo = friendRequestRepo;
        this.userRepo = userRepo;
        this.gameService = gameService;
        this.wordleRepo = wordleRepo;
    }

    @Override
    public RequestOutDto sendRequest(Long senderId, RequestInDto requestInDto) throws ResourceNotFoundException, ResourceConflictException, ForbiddenRequestException, InterruptedException {
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
        request = matchRequestRepo.save(request);
        Instant getTo = Instant.now().plus(Duration.ofMinutes(2));
        while(Instant.now().isBefore(getTo)) {
            request = matchRequestRepo.findById(request.getId()).get();
            if (request.getState().equals(ReqState.Accepted)) {
                Wordle w = wordleRepo.getCrr(request.getMatch().getId(), senderId);
                return RequestMapper.requestToReqOutDto(request, w.getId());
            }
            else if (request.getState().equals(ReqState.Rejected))
                break;
            TimeUnit.SECONDS.sleep(1);
        }
        int is = matchRequestRepo.delSpecial(request.getId());
        if(is==0)
        {
            request = matchRequestRepo.findById(request.getId()).get();
            if (request.getState().equals(ReqState.Accepted)) {
                Wordle w = wordleRepo.getCrr(request.getMatch().getId(), senderId);
                return RequestMapper.requestToReqOutDto(request, w.getId());
            }
            else
                return RequestMapper.requestToReqOutDto(request);
        }
        request.setState(ReqState.Rejected);
        return RequestMapper.requestToReqOutDto(request);
    }

    @Transactional
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
        req.setAnswerTime(Instant.now());
        if(state==ReqState.Accepted)
        {
            Match match = gameService.createGame(req);
            req.setMatch(match);
            matchRequestRepo.save(req);
            Wordle w = wordleRepo.getCrr(match.getId(),receiver);
            return RequestMapper.requestToReqOutDto(req,w.getId());
        }
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

    @Scheduled(fixedRate = 6,timeUnit = TimeUnit.MINUTES)
    public void delAllStuck() {
        matchRequestRepo.delAllStuck();
    }

}
