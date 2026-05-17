package com.photostudio.photostudio_backend.dto.user;

public record UserBasicOutputDTO(
        Long id,

        String name,

        String surname,

        String username
) {
}
