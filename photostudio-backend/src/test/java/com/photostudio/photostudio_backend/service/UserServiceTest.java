package com.photostudio.photostudio_backend.service;

import com.photostudio.photostudio_backend.dto.password.PasswordChangeDTO;
import com.photostudio.photostudio_backend.dto.password.PasswordResetDTO;
import com.photostudio.photostudio_backend.dto.user.MyProfileUpdateDTO;
import com.photostudio.photostudio_backend.dto.user.UserInputDTO;
import com.photostudio.photostudio_backend.dto.user.UserFullOutputDTO;
import com.photostudio.photostudio_backend.exception.BusinessException;
import com.photostudio.photostudio_backend.exception.EntityException;
import com.photostudio.photostudio_backend.exception.ValidationException;
import com.photostudio.photostudio_backend.model.token.PasswordResetToken;
import com.photostudio.photostudio_backend.model.User;
import com.photostudio.photostudio_backend.model.enums.UserRole;
import com.photostudio.photostudio_backend.model.token.VerificationToken;
import com.photostudio.photostudio_backend.repository.PasswordResetTokenRepository;
import com.photostudio.photostudio_backend.repository.UserRepository;
import com.photostudio.photostudio_backend.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MailNotificationService mailNotificationService;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EventService eventService;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Captor
    private ArgumentCaptor<VerificationToken> tokenArgumentCaptor;

    private User testUser;
    private User adminUser;
    private User secondAdminUser;
    private User superAdminUser;
    private User moderatorUser;

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                passwordEncoder,
                mailNotificationService,
                verificationTokenRepository,
                "http://test-app.com",
                passwordResetTokenRepository

        );
        testUser = new User("Test", "User", "test@user.com", "oldHash",
                "testUser", "123456789", UserRole.USER);
        testUser.setEnabled(true);

        adminUser = new User("Admin", "Admin", "admin@user.com", "adminHash",
                "adminUser", "987654321", UserRole.ADMIN);
        adminUser.setEnabled(true);

        secondAdminUser = new User("Admin", "Admin", "admin@user.com", "adminHash",
                "adminUser2", "187654321", UserRole.ADMIN);
        secondAdminUser.setEnabled(true);

        superAdminUser = new User("Admin", "Admin", "admin@user.com", "adminHash",
                "adminUser", "987654321", UserRole.SUPER_ADMIN);
        superAdminUser.setEnabled(true);

        moderatorUser = new User("Admin", "Admin", "admin@user.com", "adminHash",
                "adminUser", "987654321", UserRole.MODERATOR);
        moderatorUser.setEnabled(true);

        setId(adminUser, 1L);
        setId(testUser, 2L);
        setId(secondAdminUser, 3L);
        setId(superAdminUser, 4L);
        setId(moderatorUser, 5L);
    }
    private void mockSecurityContext(User user) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(user.getUsername());

        when(userRepository.findByUsernameAndDeletedIsFalseAndEnabledIsTrue(user.getUsername()))
                .thenReturn(Optional.of(user));
    }

    @Nested
    @DisplayName("createUser Tests")
    class CreateUserTests {

        private UserInputDTO validDTO;

        @BeforeEach
        void setUp() {
            validDTO = new UserInputDTO(0L, "John", "Snow", "new@ksaf.pl", "Password123!",
                    "Password123!", "newUser", "111222333");
        }

        @Test
        @DisplayName("should create user successfully")
        void shouldCreateUserSuccessfully() {
            // Given
            when(userRepository.existsByEmailAndDeletedIsFalse(anyString())).thenReturn(false);
            when(userRepository.existsByUsernameAndDeletedIsFalse(anyString())).thenReturn(false);
            when(userRepository.existsByPhoneNumberAndDeletedIsFalse(anyString())).thenReturn(false);
            when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword");

            // When
            userService.createUser(validDTO);

            // Then
            verify(userRepository).save(userArgumentCaptor.capture());
            User savedUser = userArgumentCaptor.getValue();

            assertEquals("John", savedUser.getName());
            assertEquals("Snow", savedUser.getSurname());
            assertEquals("new@ksaf.pl", savedUser.getEmail());
            assertEquals("hashedPassword", savedUser.getPasswordHash());
            assertEquals("newUser", savedUser.getUsername());
            assertEquals("111222333", savedUser.getPhoneNumber());
            assertEquals(UserRole.USER, savedUser.getRole());
            assertFalse(savedUser.isEnabled());

            verify(verificationTokenRepository).save(tokenArgumentCaptor.capture());
            VerificationToken savedToken = tokenArgumentCaptor.getValue();
            assertEquals(savedUser, savedToken.getUser());
            assertNotNull(savedToken.getToken());

            verify(mailNotificationService).sendMailVerificationMail(
                    eq("new@ksaf.pl"),
                    contains("http://test-app.com/auth/confirm?token=")
            );
        }

        @Test
        @DisplayName("should throw exception when passwords do not match")
        void shouldThrowExceptionWhenPasswordsDoNotMatch() {
            // Given
            UserInputDTO badDTO = new UserInputDTO(0L, "John", "Snow", "new@ksaf.pl", "Password123!",
                    "Password125!", "newUser", "111222333");

            // When / Then
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                userService.createUser(badDTO);
            });
            assertEquals("Passwords do not match", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when email is taken")
        void shouldThrowExceptionWhenEmailIsTaken() {
            // Given
            when(userRepository.existsByEmailAndDeletedIsFalse("new@ksaf.pl")).thenReturn(true);

            // When / Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.createUser(validDTO);
            });
            assertEquals("Email already exists", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when username is taken")
        void shouldThrowExceptionWhenUsernameIsTaken() {
            // Given
            when(userRepository.existsByEmailAndDeletedIsFalse(anyString())).thenReturn(false);
            when(userRepository.existsByPhoneNumberAndDeletedIsFalse(anyString())).thenReturn(false);
            when(userRepository.existsByUsernameAndDeletedIsFalse("newUser")).thenReturn(true);

            // When / Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.createUser(validDTO);
            });
            assertEquals("Username already exists", exception.getMessage());
        }
        @Test
        @DisplayName("should throw exception when phone number is taken")
        void shouldThrowExceptionWhenPhoneNumberIsTaken() {
            // Given
            when(userRepository.existsByPhoneNumberAndDeletedIsFalse("111222333")).thenReturn(true);
            when(userRepository.existsByEmailAndDeletedIsFalse(anyString())).thenReturn(false);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.createUser(validDTO);
            });
            assertEquals("Phone number already exists", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("verifyUser Tests")
    class VerifyUserTests {

        private VerificationToken token;

        @BeforeEach
        void setUp() {
            testUser.setEnabled(false);
            token = new VerificationToken("valid-token", testUser);
        }

        @Test
        void shouldVerifyUserSuccessfully() throws BusinessException {
            when(verificationTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

            userService.verifyUser("valid-token");

            verify(userRepository).save(userArgumentCaptor.capture());
            User verifiedUser = userArgumentCaptor.getValue();

            assertTrue(verifiedUser.isEnabled());
            verify(verificationTokenRepository).delete(token);

        }
        @Test
        @DisplayName("should throw exception for invalid token")
        void shouldThrowExceptionForInvalidToken() {
            // Given
            when(verificationTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

            // When / Then
            EntityException exception = assertThrows(EntityException.class, () -> {
                userService.verifyUser("invalid-token");
            });
            assertEquals("Invalid verification token", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception and delete user/token if expired")
        void shouldThrowExceptionAndDeletionsWhenTokenIsExpired() {
            // Given
            token.setExpiryDate(LocalDateTime.now().minusHours(1));
            when(verificationTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.verifyUser("expired-token");
            });
            assertEquals("Token is no longer valid", exception.getMessage());

            verify(verificationTokenRepository).delete(token);
            verify(userRepository).delete(testUser);
            verify(userRepository, never()).save(any());
        }
    }
    @Nested
    @DisplayName("Delete Account Tests (Admin and User)")
    class DeleteAccountTests {

        @Test
        @DisplayName("[Admin] should delete user successfully")
        void adminShouldDeleteUserSuccessfully() throws BusinessException {
            // Given
            mockSecurityContext(adminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(2L)).thenReturn(Optional.of(testUser));

            // When
            userService.deleteUser(2L);

            // Then
            verify(userRepository).save(userArgumentCaptor.capture());
            User deletedUser = userArgumentCaptor.getValue();

            assertTrue(deletedUser.isDeleted());
            assertEquals("None", deletedUser.getName());
            assertEquals("deleted_user_2@example.com", deletedUser.getEmail());
            assertEquals("deleted_user_2", deletedUser.getUsername());
            assertNull(deletedUser.getPhoneNumber());
        }

        @Test
        @DisplayName("[Admin] should throw exception when deleting self")
        void adminShouldThrowExceptionWhenDeletingSelf() {
            // Given
            mockSecurityContext(adminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(1L)).thenReturn(Optional.of(adminUser));

            // When / Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.deleteUser(1L);
            });

            assertEquals("Admin can not delete himself", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("[Admin] should throw exception when deleting another admin")
        void adminShouldThrowExceptionWhenDeletingAnotherAdmin() {

            mockSecurityContext(adminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(3L)).thenReturn(Optional.of(secondAdminUser));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.deleteUser(3L);
            });

            assertEquals("Admin can not delete other admin", exception.getMessage());
            verify(userRepository, never()).save(any());
        }
        @Test
        @DisplayName("[Admin] cannot delete SUPER_ADMIN")
        void adminShouldThrowExceptionWhenDeletingSuperAdmin() {
            mockSecurityContext(adminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(4L)).thenReturn(Optional.of(superAdminUser));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.deleteUser(4L);
            });

            assertEquals("No one can modify Super Admin", exception.getMessage());
            verify(userRepository, never()).save(any());
        }
        @Test
        @DisplayName("[SUPER_ADMIN] can delete admin")
        void superAdminShouldDeleteAdmin() {
            mockSecurityContext(superAdminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(3L)).thenReturn(Optional.of(secondAdminUser));

            userService.deleteUser(3L);

            verify(userRepository).save(userArgumentCaptor.capture());
            User deletedUser = userArgumentCaptor.getValue();

            assertTrue(deletedUser.isDeleted());
            assertEquals("None", deletedUser.getName());
            assertEquals("deleted_user_3@example.com", deletedUser.getEmail());
            assertEquals("deleted_user_3", deletedUser.getUsername());
            assertNull(deletedUser.getPhoneNumber());
        }
    }
    @Nested
    @DisplayName("Delete Your Own Account Tests (Admin, Moderator and User)")
    class DeleteOwnAccountTests {

        @Test
        @DisplayName("[User] can delete his account")
        void userShouldDeleteHimself() {

            mockSecurityContext(adminUser);

            userService.deleteYourAccount();

            verify(userRepository).save(userArgumentCaptor.capture());
            User deletedUser = userArgumentCaptor.getValue();

            assertTrue(deletedUser.isDeleted());
            assertEquals("None", deletedUser.getName());
            assertEquals("deleted_user_1@example.com", deletedUser.getEmail());
            assertEquals("deleted_user_1", deletedUser.getUsername());
            assertNull(deletedUser.getPhoneNumber());
        }
    }
    @Nested
    @DisplayName("changeMyPassword Tests")
    class ChangeMyPasswordTests {


        @Test
        @DisplayName("should change password successfully")
        void shouldChangePasswordSuccessfully() {
            // Given
            mockSecurityContext(testUser);
            PasswordChangeDTO dto = new PasswordChangeDTO("oldPassword1!", "newPassword1!", "newPassword1!");
            when(passwordEncoder.matches("oldPassword1!", "oldHash")).thenReturn(true);
            when(passwordEncoder.encode("newPassword1!")).thenReturn("newHashedPassword");

            userService.changeMyPassword(dto);

            verify(userRepository).save(userArgumentCaptor.capture());
            User updatedUser = userArgumentCaptor.getValue();
            assertEquals("newHashedPassword", updatedUser.getPasswordHash());
        }

        @Test
        @DisplayName("should throw exception when new passwords do not match")
        void shouldThrowExceptionWhenNewPasswordsDoNotMatch() {
            // Given
            mockSecurityContext(testUser);
            PasswordChangeDTO dto = new PasswordChangeDTO("oldPassword1!", "newPass1!", "newPass2!");

            // When / Then
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                userService.changeMyPassword(dto);
            });
            assertEquals("New passwords do not match", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when old password is incorrect")
        void shouldThrowExceptionWhenOldPasswordIsIncorrect() {
            // Given
            mockSecurityContext(testUser);
            PasswordChangeDTO dto = new PasswordChangeDTO("WRONGoldPassword1!", "newPassword1!", "newPassword1!");
            when(passwordEncoder.matches("WRONGoldPassword1!", "oldHash")).thenReturn(false);

            // When / Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.changeMyPassword(dto);
            });
            assertEquals("Incorrect old password", exception.getMessage());
            verify(userRepository, never()).save(any());
        }
    }
    @Nested
    @DisplayName("updateUser (My Profile) Tests")
    class UpdateUserTests {

        @BeforeEach
        void setUp() {
            mockSecurityContext(testUser);
        }

        @Test
        @DisplayName("should update user profile successfully")
        void shouldUpdateUserSuccessfully() throws BusinessException {
            // Given
            MyProfileUpdateDTO dto = new MyProfileUpdateDTO(
                    "UpdatedName", "UpdatedSurname",
                    "999888777"
            );

            when(userRepository.existsByPhoneNumberAndDeletedIsFalse("999888777")).thenReturn(false);

            userService.updateUser(dto);

            // Then
            verify(userRepository).save(userArgumentCaptor.capture());
            User updatedUser = userArgumentCaptor.getValue();

            assertEquals("UpdatedName", updatedUser.getName());
            assertEquals("UpdatedSurname", updatedUser.getSurname());
            assertEquals("999888777", updatedUser.getPhoneNumber());
        }

        @Test
        @DisplayName("should not run validation if data is unchanged")
        void shouldNotRunValidationIfDataIsUnchanged() throws BusinessException {
            // Given
            MyProfileUpdateDTO dto = new MyProfileUpdateDTO(
                    testUser.getName(), testUser.getSurname(), testUser.getPhoneNumber()
            );

            // When
            userService.updateUser(dto);

            // Then
            verify(userRepository, never()).existsByEmailAndDeletedIsFalse(anyString());
            verify(userRepository, never()).existsByUsernameAndDeletedIsFalse(anyString());
            verify(userRepository, never()).existsByPhoneNumberAndDeletedIsFalse(anyString());

            verify(userRepository, times(1)).save(testUser);
        }

    }

    @Nested
    @DisplayName("changeUserRole Tests (New Business Logic)")
    class ChangeUserRoleTests {

        @Test
        @DisplayName("[SA] should change USER role to MODERATOR successfully")
        void saShouldChangeUserRoleToModerator() throws BusinessException {
            // Given
            mockSecurityContext(superAdminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(testUser.getId())).thenReturn(Optional.of(testUser));


            // When
            userService.changeUserRole(testUser.getId(), UserRole.MODERATOR);

            // Then
            verify(userRepository).save(userArgumentCaptor.capture());
            assertEquals(UserRole.MODERATOR, userArgumentCaptor.getValue().getRole());
        }

        @Test
        @DisplayName("[SA] should change ADMIN role to MODERATOR successfully")
        void saShouldChangeAdminRoleToModerator() throws BusinessException {
            // Given
            mockSecurityContext(superAdminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(adminUser.getId())).thenReturn(Optional.of(adminUser));

            // When
            userService.changeUserRole(adminUser.getId(), UserRole.MODERATOR);

            // Then
            verify(userRepository).save(userArgumentCaptor.capture());
            assertEquals(UserRole.MODERATOR, userArgumentCaptor.getValue().getRole());
        }

        @Test
        @DisplayName("[SA] should change MODERATOR role to ADMIN successfully")
        void saShouldPromoteModeratorToAdmin() throws BusinessException {
            // Given
            mockSecurityContext(superAdminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(moderatorUser.getId())).thenReturn(Optional.of(moderatorUser));

            // When
            userService.changeUserRole(moderatorUser.getId(), UserRole.ADMIN);

            // Then
            verify(userRepository).save(userArgumentCaptor.capture());
            assertEquals(UserRole.ADMIN, userArgumentCaptor.getValue().getRole());
        }

        @Test
        @DisplayName("[SA] should throw exception when modifying SUPER_ADMIN")
        void saShouldNotModifySuperAdmin() {
            // Given
            mockSecurityContext(superAdminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(superAdminUser.getId())).thenReturn(Optional.of(superAdminUser));

            // When / Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.changeUserRole(superAdminUser.getId(), UserRole.ADMIN);
            });
            assertEquals("No one can modify Super Admin", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("[ADMIN] should change MODERATOR role to USER successfully")
        void adminShouldChangeModeratorRoleToUser() throws BusinessException {
            // Given
            mockSecurityContext(adminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(moderatorUser.getId())).thenReturn(Optional.of(moderatorUser));

            // When
            userService.changeUserRole(moderatorUser.getId(), UserRole.USER);

            // Then
            verify(userRepository).save(userArgumentCaptor.capture());
            assertEquals(UserRole.USER, userArgumentCaptor.getValue().getRole());
        }

        @Test
        @DisplayName("[ADMIN] should throw exception when promoting to ADMIN")
        void adminShouldNotPromoteToAdmin() {
            // Given
            mockSecurityContext(adminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(testUser.getId())).thenReturn(Optional.of(testUser));

            // When / Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.changeUserRole(testUser.getId(), UserRole.ADMIN);
            });
            assertEquals("You cannot give admin or super admin role", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("[ADMIN] should throw exception when promoting to SUPER_ADMIN")
        void adminShouldNotPromoteToSuperAdmin() {
            // Given
            mockSecurityContext(adminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(testUser.getId())).thenReturn(Optional.of(testUser));

            // When / Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.changeUserRole(testUser.getId(), UserRole.SUPER_ADMIN);
            });
            assertEquals("You cannot give admin or super admin role", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("[ADMIN] should throw exception when modifying another ADMIN")
        void adminShouldNotModifyAnotherAdmin() {
            mockSecurityContext(adminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(secondAdminUser.getId())).thenReturn(Optional.of(secondAdminUser));

            // When / Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.changeUserRole(secondAdminUser.getId(), UserRole.MODERATOR);
            });
            assertEquals("You cannot modify another admin", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("[GENERAL] should throw exception when changing own role (ADMIN)")
        void shouldNotChangeOwnRoleAdmin() {
            // Given
            mockSecurityContext(adminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(adminUser.getId())).thenReturn(Optional.of(adminUser));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.changeUserRole(adminUser.getId(), UserRole.USER);
            });
            assertEquals("You cannot change your own role", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("[GENERAL] should throw exception when target is not found")
        void shouldThrowExceptionWhenUserIsNotFound() {
            // Given
            mockSecurityContext(adminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(99L)).thenReturn(Optional.empty());

            // When / Then
            EntityException exception = assertThrows(EntityException.class, () -> {
                userService.changeUserRole(99L, UserRole.MODERATOR);
            });
            assertEquals("User not found", exception.getMessage());
            verify(userRepository, never()).save(any());
        }
    }
    @Nested
    @DisplayName("changeActiveStatus Tests (New Business Logic)")
    class changeActiveStatusTests {

        @Test
        @DisplayName("Change correctly status")
        void changeCorrectlyStatus() {
            mockSecurityContext(adminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(adminUser.getId())).thenReturn(Optional.of(adminUser));

            userService.changeUserActiveMember(adminUser.getId(), false);

            assertFalse(adminUser.isActiveMember());
        }

        @Test
        @DisplayName("Cannot modify SUPER_ADMIN")
        void cannotModifySuperAdmin() {
            mockSecurityContext(adminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(superAdminUser.getId())).thenReturn(Optional.of(superAdminUser));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.changeUserActiveMember(superAdminUser.getId(), false);
            });

            assertEquals("No one can modify Super Admin", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Cannot modify not existing user")
        void cannotModifyNotExistingUser() {
            mockSecurityContext(adminUser);
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(10L)).thenReturn(Optional.empty());

            EntityException exception = assertThrows(EntityException.class, () -> {
                userService.changeUserActiveMember(10L, false);
            });

            assertEquals("User not found", exception.getMessage());
            verify(userRepository, never()).save(any());
        }
    }
    @Nested
    @DisplayName("GetUserInformation Tests (Read Operations with Sorting)")
    class GetUserInformationTests {

        private User A_BIELSKI;     // ADMIN 1
        private User A_ABACKA;      // ADMIN 2
        private User SA_NOWAK;      // SUPER_ADMIN
        private User M_KOWALSKA;    // MODERATOR
        private User U_ZORSKI;      // USER 1
        private User U_CEGLARSKI;   // USER 2
        private User U_DISABLED;    // enabled=false

        @BeforeEach
        void setUpContexts() {

            A_BIELSKI = new User("Adrian", "Bielski", "admin.a@test.com", "hashA",
                    "adminBielski", "987654321", UserRole.ADMIN);
            A_BIELSKI.setEnabled(true);

            A_ABACKA = new User("Anna", "Abacka", "admin.b@test.com", "hashB",
                    "adminAbacka", "187654321", UserRole.ADMIN);
            A_ABACKA.setEnabled(true);

            SA_NOWAK = new User("Piotr", "Nowak", "sa.nowak@test.com", "hashSA",
                    "superNowak", "999888777", UserRole.SUPER_ADMIN);
            SA_NOWAK.setEnabled(true);

            M_KOWALSKA = new User("Maria", "Kowalska", "mod.m@test.com", "hashM",
                    "modKowalska", "555444333", UserRole.MODERATOR);
            M_KOWALSKA.setEnabled(true);

            U_ZORSKI = new User("Tomasz", "Zorski", "user.z@test.com", "hashZ",
                    "userZorski", "123456789", UserRole.USER);
            U_ZORSKI.setEnabled(true);

            U_CEGLARSKI = new User("Cyprian", "Ceglarski", "user.c@test.com", "hashC",
                    "userCeglarski", "111222333", UserRole.USER);
            U_CEGLARSKI.setEnabled(true);

            U_DISABLED = new User("Nieaktywny", "Luzny", "disabled@test.com", "hashD",
                    "disabledUser", "100000000", UserRole.USER);
            U_DISABLED.setEnabled(false);
            U_DISABLED.setActiveMember(true);

            // deleted=true
            User u_DELETED = new User("Usuniety", "Koniec", "deleted@test.com", "hashE",
                    "deletedUser", "200000000", UserRole.USER);
            u_DELETED.setDeleted(true);
            u_DELETED.setEnabled(true);

            setId(A_BIELSKI, 1L);
            setId(U_ZORSKI, 2L);
            setId(A_ABACKA, 3L);
            setId(SA_NOWAK, 4L);
            setId(M_KOWALSKA, 5L);
            setId(U_CEGLARSKI, 6L);
            setId(U_DISABLED, 10L);
            setId(u_DELETED, 11L);
        }

        @Test
        @DisplayName("getAllUsers should return all active users, sorted by Role, then Surname")
        void getAllUsersShouldReturnSortedActiveUsers() {
            // Given
            List<User> allActiveUsers = List.of(SA_NOWAK, A_BIELSKI, A_ABACKA, M_KOWALSKA, U_ZORSKI, U_CEGLARSKI);
            when(userRepository.findAllByDeletedIsFalseAndEnabledIsTrue()).thenReturn(allActiveUsers);

            // When
            List<UserFullOutputDTO> users = userService.getAllUsers();

            // Then
            assertEquals(6, users.size());

            assertEquals("Nowak", users.get(0).surname());       // SUPER_ADMIN
            assertEquals("Abacka", users.get(1).surname());      // ADMIN
            assertEquals("Bielski", users.get(2).surname());     // ADMIN
            assertEquals("Kowalska", users.get(3).surname());    // MODERATOR
            assertEquals("Ceglarski", users.get(4).surname());   // USER
            assertEquals("Zorski", users.get(5).surname());      // USER
        }


        @Test
        @DisplayName("getAllUsersByRole should return users by role (USER), sorted by Surname")
        void getAllUsersByRoleShouldReturnSortedUsers() {
            List<User> userRoleUsers = List.of(U_ZORSKI, U_CEGLARSKI);
            when(userRepository.findByRoleAndDeletedIsFalseAndEnabledIsTrue(UserRole.USER))
                    .thenReturn(userRoleUsers);

            List<UserFullOutputDTO> users = userService.getAllUsersByRole(UserRole.USER);

            // Then
            assertEquals(2, users.size());

            assertEquals("Ceglarski", users.get(0).surname());
            assertEquals("Zorski", users.get(1).surname());
        }

        @Test
        @DisplayName("getAllUsersByRole should return empty list if role has no active users")
        void getAllUsersByRoleShouldReturnEmptyListIfNoMatch() {
            // Given
            when(userRepository.findByRoleAndDeletedIsFalseAndEnabledIsTrue(UserRole.MODERATOR))
                    .thenReturn(List.of());

            // When
            List<UserFullOutputDTO> users = userService.getAllUsersByRole(UserRole.MODERATOR);

            // Then
            assertTrue(users.isEmpty());
        }

        @Test
        @DisplayName("getUserById should find an active user by ID")
        void getUserByIdShouldFindActiveUser() throws BusinessException {
            // Given
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(U_ZORSKI.getId()))
                    .thenReturn(Optional.of(U_ZORSKI));

            // When
            UserFullOutputDTO dto = userService.getUserById(U_ZORSKI.getId());

            // Then
            assertEquals(U_ZORSKI.getUsername(), dto.username());
        }

        @Test
        @DisplayName("getUserById should throw BusinessException when user is disabled or deleted")
        void getUserByIdShouldThrowExceptionWhenDisabledOrDeleted() {
            when(userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(U_DISABLED.getId()))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThrows(EntityException.class, () -> {
                userService.getUserById(U_DISABLED.getId());
            }, "User not found");
        }

        @Test
        @DisplayName("getUserByUsername should find an active user by username")
        void getUserByUsernameShouldFindActiveUser() throws BusinessException {
            // Given
            when(userRepository.findByUsernameAndDeletedIsFalseAndEnabledIsTrue(U_ZORSKI.getUsername()))
                    .thenReturn(Optional.of(U_ZORSKI));

            // When
            UserFullOutputDTO dto = userService.getUserByUsername(U_ZORSKI.getUsername());

            // Then
            assertEquals(U_ZORSKI.getEmail(), dto.email());
        }

        @Test
        @DisplayName("getUserByUsername should throw BusinessException when user is not found")
        void getUserByUsernameShouldThrowExceptionWhenNotFound() {
            // Given
            when(userRepository.findByUsernameAndDeletedIsFalseAndEnabledIsTrue("nonExistentUser"))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThrows(EntityException.class, () -> {
                userService.getUserByUsername("nonExistentUser");
            }, "User not found");
        }

        @Test
        @DisplayName("getUserByEmail should find an active user by email")
        void getUserByEmailShouldFindActiveUser() throws BusinessException {
            // Given
            when(userRepository.findByEmailAndDeletedIsFalseAndEnabledIsTrue(U_ZORSKI.getEmail()))
                    .thenReturn(Optional.of(U_ZORSKI));

            // When
            UserFullOutputDTO dto = userService.getUserByEmail(U_ZORSKI.getEmail());

            // Then
            assertEquals(U_ZORSKI.getUsername(), dto.username());
        }
    }
    @Nested
    @DisplayName("Password Reset and Change Tests")
    class PasswordResetTests {

        private User U_ZORSKI_LOCAL;
        private final String VALID_EMAIL = "user.z@test.com";
        private final String VALID_TOKEN = "valid-reset-token-123";

        @BeforeEach
        void setUpResetTests() {

            U_ZORSKI_LOCAL = new User("Tomasz", "Zorski", VALID_EMAIL, "oldHash",
                    "userZorski", "123456789", UserRole.USER);
            U_ZORSKI_LOCAL.setEnabled(true);
            setId(U_ZORSKI_LOCAL, 2L);
        }

        @Test
        @DisplayName("forgotPassword should generate token, save it, and send a reset email")
        void forgotPasswordShouldGenerateTokenAndSendEmail() throws BusinessException {

            when(userRepository.findByEmailAndDeletedIsFalseAndEnabledIsTrue(VALID_EMAIL))
                    .thenReturn(Optional.of(U_ZORSKI_LOCAL));

            PasswordResetToken tempToken = new PasswordResetToken(VALID_TOKEN, U_ZORSKI_LOCAL);
            when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                    .thenReturn(tempToken);

            // When
            userService.forgotPassword(VALID_EMAIL);

            // Then
            verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));

            String BASE_URL = "http://test-app.com";
            verify(mailNotificationService).sendPasswordResetMail(
                    eq(VALID_EMAIL),
                    contains(BASE_URL + "/reset-password?token=")
            );
        }

        @Test
        @DisplayName("forgotPassword should throw EntityException if user is not found/active")
        void forgotPasswordShouldThrowExceptionIfUserNotFound() {
            // Given
            when(userRepository.findByEmailAndDeletedIsFalseAndEnabledIsTrue(anyString()))
                    .thenReturn(Optional.empty());

            assertThrows(EntityException.class, () -> userService.forgotPassword("unknown@test.com"));
            verify(passwordResetTokenRepository, never()).save(any());
            verify(mailNotificationService, never()).sendPasswordResetMail(any(), any());
        }

        @Test
        @DisplayName("changePassword should successfully reset password and delete token")
        void changePasswordShouldSucceedAndResetPassword() throws Exception {
            // Given
            PasswordResetDTO dto = new PasswordResetDTO("newPass123!", "newPass123!");
            PasswordResetToken token = new PasswordResetToken(VALID_TOKEN, U_ZORSKI_LOCAL);

            when(passwordResetTokenRepository.findByToken(VALID_TOKEN))
                    .thenReturn(Optional.of(token));

            when(passwordEncoder.encode("newPass123!")).thenReturn("newHashedPassword");

            // When
            userService.changePassword(VALID_TOKEN, dto);

            // Then
            verify(userRepository).save(userArgumentCaptor.capture());
            assertEquals("newHashedPassword", userArgumentCaptor.getValue().getPasswordHash());

            verify(passwordResetTokenRepository).delete(token);
        }

        @Test
        @DisplayName("changePassword should throw BusinessException if token is expired")
        void changePasswordShouldThrowExceptionIfTokenIsExpired() {
            // Given
            PasswordResetDTO dto = new PasswordResetDTO("newPass123", "newPass123");
            // Token, który wygasł minutę temu
            PasswordResetToken expiredToken = new PasswordResetToken(VALID_TOKEN, U_ZORSKI_LOCAL);
            expiredToken.setExpiryDate(LocalDateTime.now().minusMinutes(35));

            when(passwordResetTokenRepository.findByToken(VALID_TOKEN))
                    .thenReturn(Optional.of(expiredToken));

            // When / Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.changePassword(VALID_TOKEN, dto);
            });

            assertEquals("Token is no longer valid", exception.getMessage());
            verify(passwordResetTokenRepository).delete(expiredToken);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("changePassword should throw EntityException if token is not found")
        void changePasswordShouldThrowExceptionIfTokenNotFound() {
            // Given
            PasswordResetDTO dto = new PasswordResetDTO("newPass123", "newPass123");
            when(passwordResetTokenRepository.findByToken(anyString()))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThrows(EntityException.class, () -> userService.changePassword("non-existent-token", dto));
            verify(userRepository, never()).save(any());
            verify(passwordResetTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("changePassword should throw ValidationException if passwords do not match")
        void changePasswordShouldThrowValidationExceptionIfPasswordsMismatch() {
            // Given
            PasswordResetDTO dto = new PasswordResetDTO("newPass123", "mismatchingPass");
            PasswordResetToken token = new PasswordResetToken(VALID_TOKEN, U_ZORSKI_LOCAL);

            when(passwordResetTokenRepository.findByToken(VALID_TOKEN))
                    .thenReturn(Optional.of(token));

            // When / Then
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                userService.changePassword(VALID_TOKEN, dto);
            });

            assertEquals("Passwords do not match", exception.getMessage());
            verify(userRepository, never()).save(any());
            verify(passwordResetTokenRepository, never()).delete(any());
        }
    }
}
