package com.photostudio.photostudio_backend.dto.user;

import com.photostudio.photostudio_backend.model.enums.UserRole;

public record UserFullOutputDTO(
        Long id,

        String name,

        String surname,

        String email,

        String username,

        boolean activeMember,

        String phoneNumber,

        UserRole role
) {
}
