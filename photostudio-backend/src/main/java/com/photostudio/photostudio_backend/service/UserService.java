package com.photostudio.photostudio_backend.service;

import com.photostudio.photostudio_backend.dto.password.PasswordChangeDTO;
import com.photostudio.photostudio_backend.dto.password.PasswordResetDTO;
import com.photostudio.photostudio_backend.dto.user.MyProfileUpdateDTO;
import com.photostudio.photostudio_backend.dto.user.UserBasicOutputDTO;
import com.photostudio.photostudio_backend.dto.user.UserInputDTO;
import com.photostudio.photostudio_backend.dto.user.UserFullOutputDTO;
import com.photostudio.photostudio_backend.exception.BusinessException;
import com.photostudio.photostudio_backend.exception.EntityException;
import com.photostudio.photostudio_backend.exception.ValidationException;
import com.photostudio.photostudio_backend.model.*;
import com.photostudio.photostudio_backend.model.enums.EquipmentCategory;
import com.photostudio.photostudio_backend.model.enums.UserRole;
import com.photostudio.photostudio_backend.model.token.PasswordResetToken;
import com.photostudio.photostudio_backend.model.token.VerificationToken;
import com.photostudio.photostudio_backend.repository.PasswordResetTokenRepository;
import com.photostudio.photostudio_backend.repository.UserRepository;
import com.photostudio.photostudio_backend.repository.VerificationTokenRepository;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailNotificationService mailNotificationService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final String urlBase;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);


    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            MailNotificationService mailNotificationService,
            VerificationTokenRepository verificationTokenRepository,
            @Value("${app.base-url}") String appBaseUrl,
            PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailNotificationService = mailNotificationService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.urlBase = appBaseUrl;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Transactional
    public void createUser(@Valid UserInputDTO userDTO) {
        LOGGER.info("Attempting to create user with username: {}", userDTO.username());
        validateUniqueEmail(userDTO.email());
        validateUniquePhoneNumber(userDTO.phoneNumber());
        validateUniqueUsername(userDTO.username());

        if(!userDTO.password().equals(userDTO.confirmPassword())) {
            LOGGER.warn("Password mismatch for user creation: {}", userDTO.username());
            throw new ValidationException("Passwords do not match");
        }

        String hashedPassword = passwordEncoder.encode(userDTO.password());

        User user = new User(
                userDTO.name(),
                userDTO.surname(),
                userDTO.email(),
                hashedPassword,
                userDTO.username(),
                userDTO.phoneNumber(),
                UserRole.USER
        );

        userRepository.save(user);
        LOGGER.info("User {} saved to database with ID {}", user.getUsername(), user.getId());
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);
        String confirmationUrl = urlBase + "/auth/confirm?token=" + token;
        mailNotificationService.sendMailVerificationMail(user.getEmail(), confirmationUrl);
        LOGGER.info("Verification email sent to {}", user.getEmail());
    }

    @Transactional
    public void verifyUser(String token) {
        LOGGER.info("Attempting to verify user with token.");
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    LOGGER.warn("Invalid verification token used: {}", token);
                    return new EntityException("Invalid verification token");
                });

        User user = verificationToken.getUser();

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            LOGGER.warn("Expired verification token used for user: {}. Deleting user.", user.getUsername());
            verificationTokenRepository.delete(verificationToken);
            userRepository.delete(user);
            throw new BusinessException("Token is no longer valid");
        }
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
        LOGGER.info("User account enabled for user: {}", user.getUsername());
    }

    @Transactional
    public void forgotPassword(String email) {
        LOGGER.info("Password reset requested for email: {}", email);
        User user = userRepository.findByEmailAndDeletedIsFalseAndEnabledIsTrue(email)
                .orElseThrow(() -> {
                    LOGGER.warn("Password reset requested for non-existent email: {}", email);
                    return new EntityException("User not found");
                });

        Optional<PasswordResetToken> tokenOld = passwordResetTokenRepository.findByUserId(user.getId());
        tokenOld.ifPresent(passwordResetTokenRepository::delete);
        passwordResetTokenRepository.flush();
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(passwordResetToken);

        String resetLink = urlBase + "/reset-password?token=" + token;
        mailNotificationService.sendPasswordResetMail(user.getEmail(), resetLink);
        LOGGER.info("Password reset token generated and email sent to: {}", user.getEmail());
    }

    @Transactional
    public void changePassword(String token, PasswordResetDTO passwordDTO) {
        LOGGER.info("Attempting to change password with reset token.");
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    LOGGER.warn("Invalid password reset token used.");
                    return new EntityException("Token not found");
                });

        if(resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            LOGGER.warn("Expired password reset token used for user: {}", resetToken.getUser().getUsername());
            passwordResetTokenRepository.delete(resetToken);
            throw new BusinessException("Token is no longer valid");
        }
        if (!passwordDTO.newPassword().equals(passwordDTO.confirmNewPassword())) {
            LOGGER.warn("Password mismatch during password reset for user: {}", resetToken.getUser().getUsername());
            throw new ValidationException("Passwords do not match");
        }
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(passwordDTO.newPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
        LOGGER.info("Password successfully reset for user: {}", user.getUsername());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    protected void deleteUser(Long id) {
        User ad_position = getLoggedUser();
        LOGGER.warn("Admin {} attempting to delete user with ID: {}", ad_position.getUsername(), id);
        User user = getUserOrThrowException(id);

        if (user.getRole().equals(UserRole.SUPER_ADMIN)) {
            LOGGER.error("User {} (Admin) tried to delete SUPER_ADMIN (User ID: {})", ad_position.getUsername(), id);
            throw new BusinessException("No one can modify Super Admin");
        }
        if (ad_position.getRole().equals(UserRole.ADMIN)) {
            if (user.getId().equals(ad_position.getId())) {
                LOGGER.warn("Admin {} tried to delete themselves.", ad_position.getUsername());
                throw new BusinessException("Admin can not delete himself");
            }
            if (user.getRole().equals(UserRole.ADMIN)) {
                LOGGER.warn("Admin {} tried to delete another admin (User ID: {})", ad_position.getUsername(), id);
                throw new BusinessException("Admin can not delete other admin");
            }
        }
        deleteAccountProcedure(user);
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER')")
    @Transactional
    protected void deleteYourAccount() {
        User user = getLoggedUser();
        LOGGER.warn("User {} is deleting their own account.", user.getUsername());
        deleteAccountProcedure(user);
    }

    private void deleteAccountProcedure(User user) {
        LOGGER.info("Starting account deletion procedure for user: {} (ID: {})", user.getUsername(), user.getId());

        user.setDeleted(true);
        user.setName("None");
        user.setSurname("None");
        user.setEmail("deleted_user_" + user.getId() + "@example.com");
        user.setUsername("deleted_user_" + user.getId());
        user.setPhoneNumber(null);
        user.setPasswordHash("NoneNone");

        userRepository.save(user);
        LOGGER.info("Successfully anonymized and marked user as deleted: {} (ID: {})", user.getUsername(), user.getId());
    }

    @Transactional
    public void changeMyPassword(@Valid PasswordChangeDTO dto) {
        User user = getLoggedUser();
        LOGGER.info("User {} attempting to change their own password.", user.getUsername());
        if (!dto.newPassword().equals(dto.confirmNewPassword())) {
            LOGGER.warn("Password mismatch for user {} during 'changeMyPassword'.", user.getUsername());
            throw new ValidationException("New passwords do not match");
        }
        if (!passwordEncoder.matches(dto.oldPassword(), user.getPasswordHash())) {
            LOGGER.warn("Incorrect old password provided by user {}.", user.getUsername());
            throw new BusinessException("Incorrect old password");
        }
        user.setPasswordHash(passwordEncoder.encode(dto.newPassword()));

        userRepository.save(user);
        LOGGER.info("User {} successfully changed their password.", user.getUsername());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    public void updateUser(@Valid MyProfileUpdateDTO userDTO) {
        User user = getLoggedUser();
        LOGGER.info("User {} updating their profile.", user.getUsername());

        if(!userDTO.name().equals(user.getName())) {
            user.setName(userDTO.name());
        }
        if(!userDTO.surname().equals(user.getSurname())) {
            user.setSurname(userDTO.surname());
        }
        if(!userDTO.phoneNumber().equals(user.getPhoneNumber())) {
            validateUniquePhoneNumber(userDTO.phoneNumber());
            user.setPhoneNumber(userDTO.phoneNumber());
        }
        userRepository.save(user);
        LOGGER.info("User {} successfully updated profile.", user.getUsername());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void adminUpdateUser(Long id, @Valid com.photostudio.photostudio_backend.dto.user.AdminUserUpdateDTO dto) {
        User logged = getLoggedUser();
        LOGGER.info("Admin {} attempting to update user ID {}", logged.getUsername(), id);
        User user = getUserOrThrowException(id);

        if (user.getRole().equals(UserRole.SUPER_ADMIN)) {
            LOGGER.error("Admin {} tried to modify SUPER_ADMIN (User ID: {})", logged.getUsername(), id);
            throw new BusinessException("No one can modify Super Admin");
        }
        if (logged.getRole().equals(UserRole.ADMIN) && user.getRole().equals(UserRole.ADMIN) && !logged.getId().equals(id)) {
            LOGGER.error("Admin {} tried to modify another admin (User ID: {})", logged.getUsername(), id);
            throw new BusinessException("You cannot modify another admin");
        }
        if (dto.role().equals(UserRole.SUPER_ADMIN)) {
            LOGGER.error("Admin {} tried to modify another user to super_admin status (User ID: {})", logged.getUsername(), id);
            throw new BusinessException("You cannot modify another user to super_admin");
        }

        if (!dto.name().equals(user.getName())) {
            user.setName(dto.name());
        }
        if (!dto.surname().equals(user.getSurname())) {
            user.setSurname(dto.surname());
        }
        if (!dto.phoneNumber().equals(user.getPhoneNumber())) {
            validateUniquePhoneNumber(dto.phoneNumber());
            user.setPhoneNumber(dto.phoneNumber());
        }
        if (!dto.role().equals(user.getRole())) {
            changeUserRole(id, dto.role());
        }
        if (!dto.activeMember().equals(user.isActiveMember())) {
            user.setActiveMember(dto.activeMember());
        }

        userRepository.save(user);
        LOGGER.info("Admin {} successfully updated user ID {}", logged.getUsername(), id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void changeUserRole(Long id, UserRole role) {
        User logged = getLoggedUser();
        LOGGER.warn("Admin {} attempting to change role of user ID {} to {}", logged.getUsername(), id, role);
        User user = getUserOrThrowException(id);

        if (user.getRole().equals(UserRole.SUPER_ADMIN)) {
            LOGGER.error("Admin {} (ID: {}) tried to modify SUPER_ADMIN (User ID: {})", logged.getUsername(), logged.getId(), id);
            throw new BusinessException("No one can modify Super Admin");
        }
        if (logged.getId().equals(id)) {
            LOGGER.warn("Admin {} tried to change their own role.", logged.getUsername());
            throw new BusinessException("You cannot change your own role");
        }
        if (logged.getRole().equals(UserRole.ADMIN)) {
            if (role.equals(UserRole.ADMIN) || role.equals(UserRole.SUPER_ADMIN)) {
                LOGGER.error("Admin {} tried to grant ADMIN/SUPER_ADMIN role to user ID {}.", logged.getUsername(), id);
                throw new BusinessException("You cannot give admin or super admin role");
            }
            if (user.getRole().equals(UserRole.ADMIN)) {
                LOGGER.error("Admin {} tried to modify another admin (User ID: {}).", logged.getUsername(), id);
                throw new BusinessException("You cannot modify another admin");
            }
        }
        user.setRole(role);
        userRepository.save(user);
        LOGGER.warn("User {} role successfully changed to {} by admin {}", user.getUsername(), role, logged.getUsername());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'SUPER_ADMIN')")
    @Transactional
    public void changeUserActiveMember(Long id, Boolean activeMember){
        User loggedUser = getLoggedUser();
        LOGGER.info("User {} attempting to change active status of user ID {} to {}", loggedUser.getUsername(), id, activeMember);
        User user = getUserOrThrowException(id);
        if (user.getRole().equals(UserRole.SUPER_ADMIN)) {
            LOGGER.warn("Attempt to change active status of SUPER_ADMIN (User ID: {}) by {}", id, loggedUser.getUsername());
            throw new BusinessException("No one can modify Super Admin");
        }
        user.setActiveMember(activeMember);
        userRepository.save(user);
        LOGGER.info("Active status for user {} (ID: {}) set to {} by {}", user.getUsername(), id, activeMember, loggedUser.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailAndDeletedIsFalseAndEnabledIsTrue(email)
                .orElseThrow(() -> {
                    LOGGER.warn("Login attempt failed for non-existent, deleted, or disabled user: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
    }

    // GET INFORMATION REGARDING USER

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    public List<UserFullOutputDTO> getAllUsersByRole(UserRole role) {
        return userRepository.findByRoleAndDeletedIsFalseAndEnabledIsTrue(role).stream()
                .map(this::convertUserToOutputDTO)
                .sorted(Comparator.comparing(UserFullOutputDTO::surname))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    public List<UserFullOutputDTO> getAllUsers() {
        return userRepository.findAllByDeletedIsFalseAndEnabledIsTrue().stream()
                .map(this::convertUserToOutputDTO)
                .sorted(Comparator.comparing(UserFullOutputDTO::role).reversed().thenComparing(UserFullOutputDTO::surname))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    public UserFullOutputDTO getUserById(Long id) {
        Optional<User> user = userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(id);
        if(user.isEmpty()) {
            LOGGER.warn("User not found by ID: {}", id);
            throw new EntityException("User not found");
        }
        return convertUserToOutputDTO(user.get());
    }

    @Transactional(readOnly = true)
    public UserBasicOutputDTO getBasicUserById(Long id) {
        Optional<User> user = userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(id);
        if(user.isEmpty()) {
            LOGGER.warn("Basic user not found by ID: {}", id);
            throw new EntityException("User not found");
        }
        return convertUserToBasicOutputDTO(user.get());
    }

    @Transactional(readOnly = true)
    public UserFullOutputDTO getOwnFullUserById() {
        return convertUserToOutputDTO(getLoggedUser());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    @Transactional(readOnly = true)
    public UserFullOutputDTO getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsernameAndDeletedIsFalseAndEnabledIsTrue(username);
        if(user.isEmpty()) {
            LOGGER.warn("User not found by username: {}", username);
            throw new EntityException("User not found");
        }
        return convertUserToOutputDTO(user.get());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    public UserFullOutputDTO getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmailAndDeletedIsFalseAndEnabledIsTrue(email);
        if(user.isEmpty()) {
            LOGGER.warn("User not found by email: {}", email);
            throw new EntityException("User not found");
        }
        return convertUserToOutputDTO(user.get());
    }

    // METHODS TO VERIFY CORRECTNESS OF DATA

    protected User getUserOrThrowException(Long id) {
        return userRepository.findByIdAndDeletedIsFalseAndEnabledIsTrue(id)
                .orElseThrow(() -> {
                    LOGGER.warn("User not found for ID: {}", id);
                    return new EntityException("User not found");
                });
    }

    protected User getLoggedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsernameAndDeletedIsFalseAndEnabledIsTrue(username)
                .orElseThrow(() -> {
                    LOGGER.error("Security context holder has username '{}' but user is not in DB or not active/deleted.", username);
                    return new EntityException("User context not found");
                });
    }
    private void validateUniqueEmail(String email) {
        if(userRepository.existsByEmailAndDeletedIsFalse(email)) {
            LOGGER.warn("Validation failed: Email already exists: {}", email);
            throw new ValidationException("Email already exists");
        }
    }

    private void validateUniqueUsername(String username) {
        if(userRepository.existsByUsernameAndDeletedIsFalse(username)) {
            LOGGER.warn("Validation failed: Username already exists: {}", username);
            throw new ValidationException("Username already exists");
        }
    }

    private void validateUniquePhoneNumber(String phoneNumber) {
        if(userRepository.existsByPhoneNumberAndDeletedIsFalse(phoneNumber)) {
            LOGGER.warn("Validation failed: Phone number already exists: {}", phoneNumber);
            throw new ValidationException("Phone number already exists");
        }
    }

    protected UserFullOutputDTO convertUserToOutputDTO(User user) {
        return new UserFullOutputDTO(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getUsername(),
                user.isActiveMember(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }
    protected UserBasicOutputDTO convertUserToBasicOutputDTO(User user) {
        return new UserBasicOutputDTO(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getUsername()
        );
    }
    @Transactional
    public void changeLimits(Long userId, EquipmentCategory category, Long newLimit) {
        User user = getUserOrThrowException(userId);
        Map<EquipmentCategory, Long> limits = user.getReservationLimits();
        limits.put(category, newLimit);
        user.setReservationLimits(limits);
    }

    @Transactional(readOnly = true)
    public Map<EquipmentCategory, Long> getLimits(Long userId) {
        User user = getUserOrThrowException(userId);
        Map<EquipmentCategory, Long> limits = new EnumMap<>(EquipmentCategory.class);
        for (EquipmentCategory category : EquipmentCategory.values()) {
            limits.put(category, user.getReservationLimits().getOrDefault(category, 1000L));
        }
        return limits;
    }
}