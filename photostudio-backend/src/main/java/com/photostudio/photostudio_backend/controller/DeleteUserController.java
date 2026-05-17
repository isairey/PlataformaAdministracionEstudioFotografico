package com.photostudio.photostudio_backend.controller;

import com.photostudio.photostudio_backend.service.DeleteUserFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class DeleteUserController {

    private final DeleteUserFacade deleteUserFacade;

    public DeleteUserController(DeleteUserFacade deleteUserFacade) {
        this.deleteUserFacade = deleteUserFacade;
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER')")
    public ResponseEntity<Void> deleteYourAccount() {
        deleteUserFacade.deleteYourAccount();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        deleteUserFacade.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}