package com.teamvault.event.listener;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.teamvault.entity.UserRoleChangeLog;
import com.teamvault.event.model.UserRoleChangeEvent;
import com.teamvault.repository.UserRoleChangeLogRepository;

@Component
public class UserChangeListener {

    @Autowired
    private UserRoleChangeLogRepository logRepository;

    @EventListener
    public void handleUserRoleChanged(UserRoleChangeEvent event) {
    	
        UserRoleChangeLog log = UserRoleChangeLog.builder()
                .targetUserId(event.getTargetUser().getId())
                .targetUserName(event.getTargetUser().getCredentials().getUserName())
                .oldRole(event.getOldRole().name())
                .newRole(event.getNewRole().name())
                .changedByUserId(event.getChangedBy().getUserId())
                .changedByUserName(event.getChangedBy().getUsername())
                .action(event.getAction())
                .timestamp(Instant.now())
                .build();

        logRepository.save(log);
    }
}