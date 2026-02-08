package com.saulpos.server.security.service;

import com.saulpos.server.security.model.AuthAuditEventEntity;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.AuthAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthAuditService {

    private final AuthAuditEventRepository authAuditEventRepository;

    @Transactional
    public void loginSuccess(UserAccountEntity user) {
        saveEvent(user, user.getUsername(), "LOGIN", "SUCCESS", null);
    }

    @Transactional
    public void loginFailure(String username, UserAccountEntity user, String reason) {
        saveEvent(user, username, "LOGIN", "FAILURE", reason);
    }

    @Transactional
    public void logout(UserAccountEntity user) {
        saveEvent(user, user.getUsername(), "LOGOUT", "SUCCESS", null);
    }

    private void saveEvent(UserAccountEntity user,
                           String username,
                           String eventType,
                           String outcome,
                           String reason) {
        AuthAuditEventEntity eventEntity = new AuthAuditEventEntity();
        eventEntity.setUser(user);
        eventEntity.setUsername(username);
        eventEntity.setEventType(eventType);
        eventEntity.setOutcome(outcome);
        eventEntity.setReason(reason);
        authAuditEventRepository.save(eventEntity);
    }
}
