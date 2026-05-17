package com.photostudio.photostudio_backend.dto.eventRequest;

import com.photostudio.photostudio_backend.model.enums.ReservationStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EventRequestFilterDTO(

        @Size(max = 50)
        String search,

        ReservationStatus status,

        LocalDate dateFrom,

        LocalDate dateTo,

        @Min(0)
        int page,

        @Min(5)
        @Max(100)
        int pageSize
) {
}
