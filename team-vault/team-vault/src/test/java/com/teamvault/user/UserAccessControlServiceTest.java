package com.teamvault.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;


import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.teamvault.entity.User;
import com.teamvault.enums.UserRole;
import com.teamvault.models.CustomPrincipal;
import com.teamvault.repository.UserRepository;
import com.teamvault.security.filter.SecurityUtil;
import com.teamvault.security.filter.UserAccessControlService;


@ExtendWith(MockitoExtension.class)
public class UserAccessControlServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAccessControlService accessControlService;

    private MockedStatic<SecurityUtil> mockCurrentUser(String userId, UserRole role) {
    	
        CustomPrincipal principal = mock(CustomPrincipal.class);
        lenient().when(principal.getUserId()).thenReturn(userId);
        lenient().when(principal.getRole()).thenReturn(role.name());

        MockedStatic<SecurityUtil> mockedStatic = mockStatic(SecurityUtil.class);
        mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(principal);

        return mockedStatic;
    }

    private User mockTargetUser(String userId, UserRole role) {
    	
        User user = mock(User.class);
        lenient().when(user.getId()).thenReturn(userId);
        lenient().when(user.getUserRole()).thenReturn(role);
        return user;
    }


    @Test
    public void canPromoteUser_whenCurrentUserIsTargetUser_shouldReturnFalse() {
    	
        try (MockedStatic<SecurityUtil> securityUtilMock = mockCurrentUser("user-1", UserRole.ADMIN)) {

            boolean allowed = accessControlService.canPromoteUser("user-1");

            assertFalse(allowed);
            verifyNoInteractions(userRepository);
        }
    }

    @Test
    public void canPromoteUser_whenTargetUserDoesNotExist_shouldReturnFalse() {
    	
        try (MockedStatic<SecurityUtil> securityUtilMock = mockCurrentUser("admin-1", UserRole.ADMIN)) {

            when(userRepository.findById("user-2")).thenReturn(Optional.empty());

            boolean allowed = accessControlService.canPromoteUser("user-2");

            assertFalse(allowed);
            verify(userRepository).findById("user-2");
        }
    }

    @Test
    public void canPromoteUser_whenTargetUserIsSuperAdmin_shouldReturnFalse() {
    	
        User targetUser = mockTargetUser("user-2", UserRole.SUPER_ADMIN);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockCurrentUser("admin-1", UserRole.ADMIN)) {

            when(userRepository.findById("user-2")).thenReturn(Optional.of(targetUser));

            boolean allowed = accessControlService.canPromoteUser("user-2");

            assertFalse(allowed);
        }
    }

    @Test
    public void canPromoteUser_whenCurrentUserRoleEqualsTargetRole_shouldReturnFalse() {
    	
        User targetUser = mockTargetUser("user-2", UserRole.ADMIN);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockCurrentUser("admin-1", UserRole.ADMIN)) {

            when(userRepository.findById("user-2")).thenReturn(Optional.of(targetUser));

            boolean allowed = accessControlService.canPromoteUser("user-2");

            assertFalse(allowed);
        }
    }

    @Test
    public void canPromoteUser_whenCurrentUserRoleLowerThanTargetRole_shouldReturnFalse() {
    	
        User targetUser = mockTargetUser("user-2", UserRole.ADMIN);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockCurrentUser("user-1", UserRole.USER)) {

            when(userRepository.findById("user-2")).thenReturn(Optional.of(targetUser));

            boolean allowed = accessControlService.canPromoteUser("user-2");

            assertFalse(allowed);
        }
    }

    @Test
    public void canPromoteUser_whenCurrentUserHasHigherRole_shouldReturnTrue() {
    	
        User targetUser = mockTargetUser("user-2", UserRole.USER);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockCurrentUser("admin-1", UserRole.ADMIN)) {

            when(userRepository.findById("user-2")).thenReturn(Optional.of(targetUser));

            boolean allowed = accessControlService.canPromoteUser("user-2");

            assertTrue(allowed);
        }
    }
}
