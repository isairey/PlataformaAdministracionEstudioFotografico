package com.photostudio.photostudio_backend.dto.event;

import com.photostudio.photostudio_backend.model.enums.EventType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record InputEventDTO(
    @NotNull
    LocalDate date,

    @NotNull
    LocalTime time,

    @NotBlank()
    @Size(min = 2, max = 100)
    String name,

    @Size(max = 500)
    String description,

    @NotBlank()
    @Size(min = 2, max = 50)
    String location,

    @Min(0)
    @Max(20)
    int numberOfPeopleRequired,

    @NotNull
    EventType type

    ) {}
