package com.photostudio.photostudio_backend.dto.eventRequest;

import com.photostudio.photostudio_backend.model.enums.ReservationStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventRequestDTO(
        Long id,

        Long eventId,

        Long userId,

        String eventName,

        LocalDate eventDate,

        String eventLocation,

        LocalDate createdDate,

        LocalTime createdTime,

        @Size(max = 500)
        String comment,

        ReservationStatus status
) {
}
