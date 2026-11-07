package com.h123.waddle.security;

import com.h123.waddle.entities.User;
import com.h123.waddle.entities.enums.Role;
import com.h123.waddle.repos.UserRepo;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

public class RoleAuth implements AuthorizationManager<RequestAuthorizationContext> {

    private final UserRepo userRepo;

    public RoleAuth(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext object) {
        if(!(authentication.get().getPrincipal() instanceof Long))
            return new AuthorizationDecision(false);
        Long id = (Long) authentication.get().getPrincipal();
        User u = userRepo.findById(id).get();
        if(u.getRole().equals(Role.Player))
            return new AuthorizationDecision(false);
        return new AuthorizationDecision(true);
    }
}
