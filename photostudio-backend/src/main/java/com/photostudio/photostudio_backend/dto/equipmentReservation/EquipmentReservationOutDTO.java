package com.photostudio.photostudio_backend.dto.equipmentReservation;

import com.photostudio.photostudio_backend.model.enums.EquipmentReservationStatus;

import java.time.LocalDateTime;

public record EquipmentReservationOutDTO(
        long id,
        EquipmentReservationStatus status,
        String creatorFullName,
        String eventName,
        String comment,
        LocalDateTime start,
        LocalDateTime end,
        String reviewerFullName
) {
}
