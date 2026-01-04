package com.teamvault.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.auth.Credentials;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.teamvault.entity.User;
import com.teamvault.entity.log.UserRoleChangeLog;
import com.teamvault.enums.RoleChangeAction;
import com.teamvault.enums.UserRole;
import com.teamvault.event.listener.UserChangeListener;
import com.teamvault.event.model.UserRoleChangeEvent;
import com.teamvault.models.CustomPrincipal;
import com.teamvault.repository.UserRoleChangeLogRepository;
import com.teamvault.valueobject.CredentialsVO;

@ExtendWith(MockitoExtension.class)
class UserChangeListenerTest {

    @Mock
    private UserRoleChangeLogRepository userRoleChangeLogRepository;

    @InjectMocks
    private UserChangeListener userChangeListener;

    @Test
    void handleUserRoleChanged_shouldSaveValidLog() {

        User targetUser = mock(User.class);
        CredentialsVO credentials = mock(CredentialsVO.class);

        when(targetUser.getId()).thenReturn("user-1");
        when(targetUser.getCredentials()).thenReturn(credentials);
        when(credentials.getUserName()).thenReturn("targetUser");

        CustomPrincipal changedBy = mock(CustomPrincipal.class);
        when(changedBy.getUserId()).thenReturn("admin-1");
        when(changedBy.getUsername()).thenReturn("adminUser");

        UserRoleChangeEvent event = UserRoleChangeEvent.builder()
                .targetUser(targetUser)
                .oldRole(UserRole.USER)
                .newRole(UserRole.ADMIN)
                .changedBy(changedBy)
                .action(RoleChangeAction.PROMOTE.toString())
                .build();

        ArgumentCaptor<UserRoleChangeLog> logCaptor =
                ArgumentCaptor.forClass(UserRoleChangeLog.class);

        userChangeListener.handleUserRoleChanged(event);

        verify(userRoleChangeLogRepository).save(logCaptor.capture());

        UserRoleChangeLog savedLog = logCaptor.getValue();

        assertEquals("user-1", savedLog.getTargetUserId());
        assertEquals("targetUser", savedLog.getTargetUserName());
        assertEquals("USER", savedLog.getOldRole());
        assertEquals("ADMIN", savedLog.getNewRole());
        assertEquals("admin-1", savedLog.getChangedByUserId());
        assertEquals("adminUser", savedLog.getChangedByUserName());
        assertEquals(RoleChangeAction.PROMOTE.toString(), savedLog.getAction());
        assertNotNull(savedLog.getTimestamp());
    }
}
