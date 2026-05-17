package com.photostudio.photostudio_backend.dto.equipmentReservation;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record EquipmentReservationDTO(
        long eventRequestId,
        LocalDateTime start,
        LocalDateTime end,
        List<Long> equipmentIDs,
        @Size(max = 250)
        String comment,
        boolean isPrivate,
        boolean isUrgent
) {
}
