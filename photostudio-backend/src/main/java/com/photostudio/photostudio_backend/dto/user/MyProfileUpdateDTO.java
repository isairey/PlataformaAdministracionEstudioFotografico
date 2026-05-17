package com.photostudio.photostudio_backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MyProfileUpdateDTO(
        @NotBlank()
        @Size(min = 2, max = 20, message = "Name must be between 2-20 letters")
        @Pattern(regexp = "^[A-Za-ząśćźżóęńł]+$", message = "Name must contain only letters")
        String name,

        @NotBlank()
        @Size(min = 2, max = 20, message = "Surname must be between 2-20 letters")
        @Pattern(regexp = "^[A-Za-ząśćźżóęńł]+$", message = "Surname must contain only letters")
        String surname,

        @NotBlank()
        @Pattern(regexp = "^(\\+48)?[0-9]{9}$")
        String phoneNumber
) {}