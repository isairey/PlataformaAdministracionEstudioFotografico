package com.photostudio.photostudio_backend.controller;

import com.photostudio.photostudio_backend.dto.password.PasswordChangeDTO;
import com.photostudio.photostudio_backend.dto.password.PasswordResetDTO;
import com.photostudio.photostudio_backend.dto.user.AdminUserUpdateDTO;
import com.photostudio.photostudio_backend.dto.user.MyProfileUpdateDTO;
import com.photostudio.photostudio_backend.dto.user.UserBasicOutputDTO;
import com.photostudio.photostudio_backend.dto.user.UserFullOutputDTO;
import com.photostudio.photostudio_backend.dto.user.UserInputDTO;
import com.photostudio.photostudio_backend.model.enums.EquipmentCategory;
import com.photostudio.photostudio_backend.model.enums.UserRole;
import com.photostudio.photostudio_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserFullOutputDTO> getOwnFullUserById() {
        return ResponseEntity.ok(userService.getOwnFullUserById());
    }

    @PatchMapping("/limits/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> changeLimits(@PathVariable Long id, @RequestParam Long newLimit, @RequestParam String category) {
        userService.changeLimits(id, EquipmentCategory.valueOf(category), newLimit);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/limits/{id}")
    public ResponseEntity<Map<EquipmentCategory, Long>> getLimits(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getLimits(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER', 'SUPER_ADMIN')")
    public ResponseEntity<UserBasicOutputDTO> getBasicUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getBasicUserById(id));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    public ResponseEntity<List<UserFullOutputDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/by-role")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    public ResponseEntity<List<UserFullOutputDTO>> getAllUsersByRole(@RequestParam UserRole role) {
        return ResponseEntity.ok(userService.getAllUsersByRole(role));
    }

    @GetMapping("/by-username")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    public ResponseEntity<UserFullOutputDTO> getUserByUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/by-email")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    public ResponseEntity<UserFullOutputDTO> getUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/full/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    public ResponseEntity<UserFullOutputDTO> getUserFullById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    public ResponseEntity<Void> updateMyProfile(@Valid @RequestBody MyProfileUpdateDTO userDTO) {
        userService.updateUser(userDTO);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/password")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER', 'SUPER_ADMIN')")
    public ResponseEntity<Void> changeMyPassword(@Valid @RequestBody PasswordChangeDTO dto) {
        userService.changeMyPassword(dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserInputDTO inputDTO) {
        userService.createUser(inputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/confirm")
    public ResponseEntity<Void> verifyUser(@RequestParam String token) {
        userService.verifyUser(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<Void> changePassword(
            @RequestParam String token,
            @Valid @RequestBody PasswordResetDTO passwordDTO) {
        userService.changePassword(token, passwordDTO);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> adminUpdateUser(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateDTO dto) {
        userService.adminUpdateUser(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> changeUserRole(@PathVariable Long id, @RequestParam UserRole role) {
        userService.changeUserRole(id, role);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active-member")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> changeUserActiveMember(@PathVariable Long id, @RequestParam Boolean activeMember){
        userService.changeUserActiveMember(id, activeMember);
        return ResponseEntity.noContent().build();
    }
}