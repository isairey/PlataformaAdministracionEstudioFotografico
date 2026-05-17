package com.photostudio.photostudio_backend.dto;

import com.photostudio.photostudio_backend.model.enums.EventStatus;
import com.photostudio.photostudio_backend.model.enums.EventType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record InputEventFilterDTO(

        @Min(0)
        int page,

        @Min(1)
        @Max(100)
        int pageSize,

        @Size(max = 100)
        String search,

        @Size(max = 100)
        String location,

        EventStatus status,

        EventType type,

        @NotNull
        Boolean onlyWithFreeSpots,

        LocalDate dateFrom,

        LocalDate dateTo
) {
}
