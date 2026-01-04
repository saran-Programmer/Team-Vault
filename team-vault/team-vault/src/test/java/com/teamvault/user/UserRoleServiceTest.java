package com.teamvault.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;

import com.teamvault.DTO.UserRoleChangeResponse;
import com.teamvault.entity.User;
import com.teamvault.enums.UserRole;
import com.teamvault.event.model.UserRoleChangeEvent;
import com.teamvault.exception.ResourceNotFoundException;
import com.teamvault.models.CustomPrincipal;
import com.teamvault.repository.UserRepository;
import com.teamvault.security.filter.SecurityUtil;
import com.teamvault.service.UserService;
import com.teamvault.valueobject.NameVO;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    private MockedStatic<SecurityUtil> mockCurrentUser() {

        CustomPrincipal principal = mock(CustomPrincipal.class);

        MockedStatic<SecurityUtil> mockedStatic = mockStatic(SecurityUtil.class);
        mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(principal);

        return mockedStatic;
    }

    private User mockUser(String userId, UserRole role) {

        User user = mock(User.class);

        NameVO name = mock(NameVO.class);
        when(name.getFullName()).thenReturn("Test User");

        when(user.getId()).thenReturn(userId);
        when(user.getUserRole()).thenReturn(role);
        when(user.getName()).thenReturn(name);

        return user;
    }

    @Test
    void promoteUser_whenUserExists_shouldPromoteAndPublishEvent() {

        User targetUser = mockUser("user-1", UserRole.USER);

        try (MockedStatic<SecurityUtil> ignored = mockCurrentUser()) {

            when(userRepository.findById("user-1"))
                    .thenReturn(Optional.of(targetUser));

            ResponseEntity<?> response = userService.promoteUser("user-1");

            verify(targetUser).setUserRole(UserRole.ADMIN);
            verify(userRepository).save(targetUser);
            verify(eventPublisher).publishEvent(any(UserRoleChangeEvent.class));

            UserRoleChangeResponse body =
                    (UserRoleChangeResponse) response.getBody();

            assertEquals(UserRole.USER.name(), body.getOldRole());
            assertEquals(UserRole.ADMIN.name(), body.getNewRole());
        }
    }

    @Test
    void promoteUser_whenUserNotFound_shouldThrowException() {

        when(userRepository.findById("missing"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.promoteUser("missing"));

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void depromoteUser_whenUserExists_shouldDepromoteAndPublishEvent() {

        User targetUser = mockUser("user-2", UserRole.ADMIN);

        try (MockedStatic<SecurityUtil> ignored = mockCurrentUser()) {

            when(userRepository.findById("user-2"))
                    .thenReturn(Optional.of(targetUser));

            ResponseEntity<?> response = userService.depromoteUser("user-2");

            verify(targetUser).setUserRole(UserRole.USER);
            verify(userRepository).save(targetUser);
            verify(eventPublisher).publishEvent(any(UserRoleChangeEvent.class));

            UserRoleChangeResponse body =
                    (UserRoleChangeResponse) response.getBody();

            assertEquals(UserRole.ADMIN.name(), body.getOldRole());
            assertEquals(UserRole.USER.name(), body.getNewRole());
        }
    }

    @Test
    void depromoteUser_whenUserNotFound_shouldThrowException() {

        when(userRepository.findById("missing"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.depromoteUser("missing"));

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void promoteUser_whenAdminIsPromoted_shouldBecomeSuperAdmin() {

        User targetUser = mockUser("admin-2", UserRole.ADMIN);

        try (MockedStatic<SecurityUtil> ignored = mockCurrentUser()) {

            when(userRepository.findById("admin-2"))
                    .thenReturn(Optional.of(targetUser));

            ResponseEntity<?> response = userService.promoteUser("admin-2");

            verify(targetUser).setUserRole(UserRole.SUPER_ADMIN);

            UserRoleChangeResponse body =
                    (UserRoleChangeResponse) response.getBody();

            assertEquals(UserRole.ADMIN.name(), body.getOldRole());
            assertEquals(UserRole.SUPER_ADMIN.name(), body.getNewRole());
        }
    }
}