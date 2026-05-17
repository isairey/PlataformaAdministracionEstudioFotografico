package com.photostudio.photostudio_backend.dto.equipmentReservation;

import com.photostudio.photostudio_backend.model.enums.EquipmentCategory;
import com.photostudio.photostudio_backend.model.enums.ReservationStatus;

public record EquipmentReservationItemOutDTO(
        Long id,

        String name,

        boolean activeMembers,

        boolean statutoryEvent,

        EquipmentCategory equipmentCategory,

        ReservationStatus status
) {
}
