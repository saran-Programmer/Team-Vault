package com.teamvault.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.teamvault.DTO.UserPatchRequest;
import com.teamvault.DTO.UserResponseDTO;
import com.teamvault.entity.User;
import com.teamvault.enums.UserRole;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.exception.ResourceNotFoundException;
import com.teamvault.models.CustomPrincipal;
import com.teamvault.repository.UserRepository;
import com.teamvault.security.filter.SecurityUtil;
import com.teamvault.service.UserService;
import com.teamvault.valueobject.ContactVO;
import com.teamvault.valueobject.CredentialsVO;
import com.teamvault.valueobject.NameVO;

@ExtendWith(MockitoExtension.class)
class UserCrudServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User buildBaseUser() {
        return User.builder()
            .id("user-1")
            .name(NameVO.builder()
                .firstName("OldFirst")
                .middleName("OldMiddle")
                .lastName("OldLast")
                .build())
            .credentials(CredentialsVO.builder()
                .userName("oldUsername")
                .email("old@email.com")
                .password("secret")
                .build())
            .contact(ContactVO.builder()
                .countryCode("+91")
                .phoneNumber("9999999999")
                .secondaryEmail("old.secondary@email.com")
                .build())
            .userRole(UserRole.USER)
            .build();
    }

    private MockedStatic<SecurityUtil> mockCurrentUser(String userId) {

        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getUserId()).thenReturn(userId);

        MockedStatic<SecurityUtil> mockedStatic = mockStatic(SecurityUtil.class);
        mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(principal);

        return mockedStatic;
    }

    @Test
    void patchUser_userNotFound_throwsException() {

        when(userRepository.findById("user-1"))
            .thenReturn(Optional.empty());

        UserPatchRequest req = new UserPatchRequest();

        assertThrows(
            ResourceNotFoundException.class,
            () -> userService.patchUser("user-1", req)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void patchUser_whenUpdatingOtherUser_shouldThrowInvalidActionException() {

        User user = buildBaseUser();
        when(userRepository.findById("user-1"))
            .thenReturn(Optional.of(user));

        try (MockedStatic<SecurityUtil> ignored = mockCurrentUser("admin-1")) {

            UserPatchRequest req = new UserPatchRequest();
            req.setFirstName("NewFirst");

            InvalidActionException ex = assertThrows(
                InvalidActionException.class,
                () -> userService.patchUser("user-1", req)
            );

            assertEquals("Not allowed to update other users", ex.getMessage());
            verify(userRepository, never()).save(any());
        }
    }

    @Test
    void patchUser_firstName_updated() {

        User user = buildBaseUser();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        try (MockedStatic<SecurityUtil> ignored = mockCurrentUser("user-1")) {

            UserPatchRequest req = new UserPatchRequest();
            req.setFirstName("NewFirst");

            User updated = userService.patchUser("user-1", req);

            assertEquals("NewFirst", updated.getName().getFirstName());
            verify(userRepository).save(user);
        }
    }

    @Test
    void patchUser_lastName_updated() {

        User user = buildBaseUser();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        try (MockedStatic<SecurityUtil> ignored = mockCurrentUser("user-1")) {

            UserPatchRequest req = new UserPatchRequest();
            req.setLastName("NewLast");

            User updated = userService.patchUser("user-1", req);

            assertEquals("NewLast", updated.getName().getLastName());
            verify(userRepository).save(user);
        }
    }

    @Test
    void patchUser_email_updated() {

        User user = buildBaseUser();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        try (MockedStatic<SecurityUtil> ignored = mockCurrentUser("user-1")) {

            UserPatchRequest req = new UserPatchRequest();
            req.setEmail("new@email.com");

            User updated = userService.patchUser("user-1", req);

            assertEquals("new@email.com", updated.getCredentials().getEmail());
            verify(userRepository).save(user);
        }
    }

    @Test
    void patchUser_userName_updated() {

        User user = buildBaseUser();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        try (MockedStatic<SecurityUtil> ignored = mockCurrentUser("user-1")) {

            UserPatchRequest req = new UserPatchRequest();
            req.setUserName("newUsername");

            User updated = userService.patchUser("user-1", req);

            assertEquals("newUsername", updated.getCredentials().getUserName());
            verify(userRepository).save(user);
        }
    }

    @Test
    void patchUser_noChange_saveNotCalled() {

        User user = buildBaseUser();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        try (MockedStatic<SecurityUtil> ignored = mockCurrentUser("user-1")) {

            UserPatchRequest req = new UserPatchRequest();
            req.setFirstName("OldFirst");

            User result = userService.patchUser("user-1", req);

            assertSame(user, result);
            verify(userRepository, never()).save(any());
        }
    }

    @Test
    void patchUser_allFields_updated() {

        User user = buildBaseUser();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        try (MockedStatic<SecurityUtil> ignored = mockCurrentUser("user-1")) {

            UserPatchRequest req = new UserPatchRequest();
            req.setFirstName("NewFirst");
            req.setLastName("NewLast");
            req.setEmail("new@email.com");
            req.setUserName("newUsername");

            User updated = userService.patchUser("user-1", req);

            assertAll(
                () -> assertEquals("NewFirst", updated.getName().getFirstName()),
                () -> assertEquals("NewLast", updated.getName().getLastName()),
                () -> assertEquals("new@email.com", updated.getCredentials().getEmail()),
                () -> assertEquals("newUsername", updated.getCredentials().getUserName())
            );

            verify(userRepository).save(user);
        }
    }
    
    @Test
    void getUserDTOById_userExists_returnsUserResponseDTO() {

        User user = buildBaseUser();
        when(userRepository.findById("user-1"))
            .thenReturn(Optional.of(user));

        UserResponseDTO response =
            userService.getUserDTOById("user-1");

        assertNotNull(response);
        assertEquals("user-1", response.getId());
        assertEquals("OldFirst OldMiddle OldLast", response.getFullName());
        assertEquals("old@email.com", response.getEmail());
        assertEquals("USER", response.getUserRole());

        verify(userRepository).findById("user-1");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserDTOById_userNotFound_throwsResourceNotFoundException() {

        when(userRepository.findById("user-1"))
            .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, 
        		() -> userService.getUserDTOById("user-1"));


        assertEquals("RESOURCE_NOT_FOUND", ex.getErrorType());

        verify(userRepository).findById("user-1");
        verifyNoMoreInteractions(userRepository);
    }

}
