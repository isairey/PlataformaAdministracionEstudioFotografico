package com.photostudio.photostudio_backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserInputDTO(

        Long id,

        @NotBlank()
        @Size(min = 2, max = 20, message = "Name must be between 2-20 letters")
        @Pattern(regexp = "^[A-Za-ząśćźżóęńł]+$", message = "Name must contain only letters")
        String name,

        @NotBlank()
        @Size(min = 2, max = 20, message = "Surname must be between 2-20 letters")
        @Pattern(regexp = "^[A-Za-ząśćźżóęńł]+$", message = "Surname must contain only letters")
        String surname,

        @NotBlank()
        @Pattern(regexp = "^[a-zA-Z_.0-9]+@[a-z]+.[a-z]+$")
        String email,

        @NotBlank()
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
        @Size(min = 8, message = "Password must have at least 8 characters")
        String password,

        @NotBlank()
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
        @Size(min = 8, message = "Password must have at least 8 characters")
        String confirmPassword,

        @NotBlank()
        @Size(min = 5, max = 20, message = "Invalid username")
        @Pattern(regexp = "^[A-Za-z0-9_.]+$", message = "Invalid username")
        String username,

        @NotBlank()
        @Pattern(regexp = "^(\\+48)?[0-9]{9}$")
        String phoneNumber
) {}
